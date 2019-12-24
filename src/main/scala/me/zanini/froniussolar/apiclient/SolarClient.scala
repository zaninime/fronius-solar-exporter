package me.zanini.froniussolar.apiclient

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.auto._
import me.zanini.froniussolar.Site
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client

trait SolarClient[F[_]] {
  def getPowerFlowRealtimeData: F[GetPowerFlowRealtimeDataResponse.Root]
}

class Http4sSolarClient[F[_]: Sync](client: Client[F], site: Site)
    extends SolarClient[F] {
  private val apiUrl = site.baseUrl / "solar_api" / "v1"

  implicit def modeDecoder: Decoder[GetPowerFlowRealtimeDataResponse.Mode] =
    Decoder[String].emap {
      case "produce-only" =>
        Right(GetPowerFlowRealtimeDataResponse.ProduceOnly)
      case "meter" =>
        Right(GetPowerFlowRealtimeDataResponse.Meter)
      case "vague-meter" =>
        Right(GetPowerFlowRealtimeDataResponse.VagueMeter)
      case "bidirectional" =>
        Right(GetPowerFlowRealtimeDataResponse.Bidirectional)
      case "ac-coupled" =>
        Right(GetPowerFlowRealtimeDataResponse.ACCoupled)
      case _ => Left("Invalid mode")
    }

  override def getPowerFlowRealtimeData
    : F[GetPowerFlowRealtimeDataResponse.Root] = {
    client.expect[GetPowerFlowRealtimeDataResponse.Root](
      apiUrl / "GetPowerFlowRealtimeData.fcgi")
  }
}
