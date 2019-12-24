package me.zanini.froniussolar.metrics

import cats.Parallel
import cats.effect.{Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import me.zanini.froniussolar.Site
import me.zanini.froniussolar.apiclient.{
  GetPowerFlowRealtimeDataResponse,
  Http4sSolarClient
}
import org.http4s.client.Client
import cats.syntax.apply._
import cats.instances.list._

import scala.concurrent.duration._

trait Poller[F[_]] {
  def run(interval: FiniteDuration): F[Unit]
}

class PollerImpl[F[_]: Sync: Timer: Logger: Parallel](httpClient: Client[F],
                                                      site: Site)
    extends Poller[F] {
  val solarClient = new Http4sSolarClient[F](httpClient, site)
  val updateService = new UpdateServiceImpl[F](site)

  override def run(interval: FiniteDuration): F[Unit] = {
    val stream = (Stream(()) ++
      Stream
        .constant(())
        .lift[F]
        .metered(interval))
      .evalMap { _ =>
        Sync[F].attempt(solarClient.getPowerFlowRealtimeData)
      }
      .flatMap(result =>
        Stream.evals {
          result match {
            case Right(value) =>
              Logger[F]
                .debug(s"Successfully queried site ${site.name}") *> Sync[F]
                .delay(List(value))
            case Left(error) =>
              Logger[F]
                .warn(error)(s"Failed querying site ${site.name}") *> Sync[F]
                .delay(List[GetPowerFlowRealtimeDataResponse.Root]())
          }
      })
      .evalMap(updateService.from)

    stream.compile.drain
  }
}
