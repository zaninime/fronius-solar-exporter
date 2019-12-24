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
      case _ => Left("Invalid site mode")
    }

  implicit def meterLocationDecoder
    : Decoder[GetPowerFlowRealtimeDataResponse.MeterLocation] =
    Decoder[String].emap {
      case "grid"    => Right(GetPowerFlowRealtimeDataResponse.Grid)
      case "meter"   => Right(GetPowerFlowRealtimeDataResponse.Load)
      case "unknown" => Right(GetPowerFlowRealtimeDataResponse.Unknown)
      case _         => Left("Invalid meter location")
    }

  implicit def batteryModeDecoder
    : Decoder[GetPowerFlowRealtimeDataResponse.BatteryMode] =
    Decoder[String].emap {
      case "disabled"     => Right(GetPowerFlowRealtimeDataResponse.Disabled)
      case "normal"       => Right(GetPowerFlowRealtimeDataResponse.Normal)
      case "service"      => Right(GetPowerFlowRealtimeDataResponse.Service)
      case "charge boost" => Right(GetPowerFlowRealtimeDataResponse.ChargeBoost)
      case "nearly depleted" =>
        Right(GetPowerFlowRealtimeDataResponse.NearlyDepleted)
      case "suspended"    => Right(GetPowerFlowRealtimeDataResponse.Suspended)
      case "calibrate"    => Right(GetPowerFlowRealtimeDataResponse.Calibrate)
      case "grid support" => Right(GetPowerFlowRealtimeDataResponse.GridSupport)
      case "deplete recovery" =>
        Right(GetPowerFlowRealtimeDataResponse.DepleteRecovery)
      case "non operable (voltage)" =>
        Right(GetPowerFlowRealtimeDataResponse.NonOperableVoltage)
      case "non operable (temperature)" =>
        Right(GetPowerFlowRealtimeDataResponse.NonOperableTemperature)
      case "preheating" => Right(GetPowerFlowRealtimeDataResponse.Preheating)
      case "startup"    => Right(GetPowerFlowRealtimeDataResponse.Startup)
      case _            => Left("Invalid battery mode")
    }

  override def getPowerFlowRealtimeData
    : F[GetPowerFlowRealtimeDataResponse.Root] = {
    client.expect[GetPowerFlowRealtimeDataResponse.Root](
      apiUrl / "GetPowerFlowRealtimeData.fcgi")
  }
}
