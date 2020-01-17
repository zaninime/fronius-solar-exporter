package me.zanini.froniussolar.metrics

import java.net.SocketException

import cats.Parallel
import cats.effect.{Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import me.zanini.froniussolar.Site
import me.zanini.froniussolar.apiclient.{
  GetPowerFlowRealtimeDataResponse,
  SolarClient
}
import cats.syntax.apply._
import cats.instances.list._

import scala.concurrent.duration._

trait Poller[F[_]] {
  def run(interval: FiniteDuration, warnOnNetworkError: Boolean): F[Unit]
}

class PollerImpl[F[_]: Timer: Logger: Parallel](solarClient: SolarClient[F],
                                                updateService: UpdateService[F],
                                                site: Site)(implicit F: Sync[F])
    extends Poller[F] {

  override def run(interval: FiniteDuration,
                   warnOnNetworkError: Boolean): F[Unit] = {
    val stream = (Stream(()) ++
      Stream
        .constant(())
        .lift[F]
        .metered(interval))
      .evalMap { _ =>
        F.attempt(solarClient.getPowerFlowRealtimeData)
      }
      .flatMap(
        result =>
          Stream.evals {
            result match {
              case Right(value) =>
                Logger[F]
                  .debug(s"Successfully queried site ${site.name}") *> F
                  .delay(List(value))
              case Left(error) =>
                (if (warnOnNetworkError || !error
                       .isInstanceOf[SocketException]) {
                   Logger[F]
                     .warn(error)(s"Failed querying site ${site.name}")
                 } else {
                   F.unit
                 }) *> F.delay(List[GetPowerFlowRealtimeDataResponse.Root]())
            }
        }
      )
      .evalMap(updateService.from)

    stream.compile.drain
  }
}
