package me.zanini.froniussolar.apiclient

import io.circe.Decoder

object Decoders {
  implicit val modeDecoder: Decoder[GetPowerFlowRealtimeDataResponse.Mode] =
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

  implicit val meterLocationDecoder
    : Decoder[GetPowerFlowRealtimeDataResponse.MeterLocation] =
    Decoder[String].emap {
      case "grid"    => Right(GetPowerFlowRealtimeDataResponse.Grid)
      case "meter"   => Right(GetPowerFlowRealtimeDataResponse.Load)
      case "unknown" => Right(GetPowerFlowRealtimeDataResponse.Unknown)
      case _         => Left("Invalid meter location")
    }

  implicit val batteryModeDecoder
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
}
