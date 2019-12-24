package me.zanini.froniussolar

import cats.Parallel
import cats.effect.{ExitCode, Sync, Timer}
import cats.instances.list._
import cats.syntax.parallel._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.prometheus.client.exporter.HTTPServer
import me.zanini.froniussolar.metrics.PollerImpl
import monix.eval.{Task, TaskApp}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object Boot extends TaskApp {
  implicit def logger: Logger[Task] = Slf4jLogger.getLogger

  override def run(args: List[String]): Task[ExitCode] =
    BlazeClientBuilder[Task](ExecutionContext.global).resource.use { client =>
      for {
        _ <- Logger[Task].info("Initializing")
        config <- AppConfig.readDefault
        _ <- Logger[Task].info("Starting HTTP server")
        server <- Task(new HTTPServer(8079)).start
        _ <- Logger[Task].info("Starting pollers")
        pollers <- createPollers[Task](config.sites.toList, client)
          .map(_.start)
          .parSequence
        _ <- Logger[Task].info("Ready")
        _ <- server.join
        _ <- pollers.map(_.join).parSequence
      } yield ExitCode.Success
    }

  private def createPollers[F[_]: Sync: Timer: Logger: Parallel](
      sites: List[SiteConfig],
      httpClient: Client[F]) = {
    sites.map(site =>
      new PollerImpl[F](httpClient, site).run(site.pollInterval))
  }
}
