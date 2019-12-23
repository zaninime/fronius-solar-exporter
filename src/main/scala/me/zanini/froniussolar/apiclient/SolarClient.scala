package me.zanini.froniussolar.apiclient

import cats.effect.Sync
import me.zanini.froniussolar.Site
import org.http4s.client.Client
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._

trait SolarClient[F[_]] {
  def getPowerFlowRealtimeData: F[GetPowerFlowRealtimeDataResponse.Root]
}

class Http4sSolarClient[F[_]: Sync](client: Client[F], site: Site)
    extends SolarClient[F] {
  private val apiUrl = site.baseUrl / "solar_api" / "v1"

  override def getPowerFlowRealtimeData
    : F[GetPowerFlowRealtimeDataResponse.Root] =
    client.expect[GetPowerFlowRealtimeDataResponse.Root](
      apiUrl / "GetPowerFlowRealtimeData.fcgi")
}
