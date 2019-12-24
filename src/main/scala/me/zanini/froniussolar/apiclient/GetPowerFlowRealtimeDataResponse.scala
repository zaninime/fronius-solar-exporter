package me.zanini.froniussolar.apiclient

import io.circe.Json

object GetPowerFlowRealtimeDataResponse {
  sealed trait Mode
  case object ProduceOnly extends Mode
  case object Meter extends Mode
  case object VagueMeter extends Mode
  case object Bidirectional extends Mode
  case object ACCoupled extends Mode

  sealed trait MeterLocation
  case object Load extends MeterLocation
  case object Grid extends MeterLocation
  case object Unknown extends MeterLocation

  sealed trait BatteryMode
  case object Disabled extends BatteryMode
  case object Normal extends BatteryMode
  case object Service extends BatteryMode
  case object ChargeBoost extends BatteryMode
  case object NearlyDepleted extends BatteryMode
  case object Suspended extends BatteryMode
  case object Calibrate extends BatteryMode
  case object GridSupport extends BatteryMode
  case object DepleteRecovery extends BatteryMode
  case object NonOperableVoltage extends BatteryMode
  case object NonOperableTemperature extends BatteryMode
  case object Preheating extends BatteryMode
  case object Startup extends BatteryMode

  case class Inverter(
      DT: Int,
      P: Int,
      SOC: Option[Int],
      Battery_Mode: Option[BatteryMode],
      E_Day: Option[Double],
      E_Total: Option[Double],
      E_Year: Option[Double],
  )

  case class Body(
      Data: Data
  )

  case class Data(
      Inverters: Map[String, Inverter],
      Site: Site,
      Version: String
  )

  case class Head(
      RequestArguments: Json,
      Status: Status,
      Timestamp: String
  )

  case class Root(
      Body: Body,
      Head: Head
  )

  case class Site(
      Mode: Mode,
      BatteryStandby: Option[Boolean],
      BackupMode: Option[Boolean],
      P_Akku: Option[Double],
      P_Grid: Option[Double],
      P_Load: Option[Double],
      P_PV: Option[Double],
      rel_Autonomy: Option[Double],
      rel_SelfConsumption: Option[Double],
      Meter_Location: Option[MeterLocation],
      E_Day: Option[Double],
      E_Total: Option[Double],
      E_Year: Option[Double],
  )

  case class Status(
      Code: Int,
      Reason: String,
      UserMessage: String
  )
}
