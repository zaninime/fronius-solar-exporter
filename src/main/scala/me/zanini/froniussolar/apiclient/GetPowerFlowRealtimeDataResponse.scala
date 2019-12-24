package me.zanini.froniussolar.apiclient

import io.circe.Json

object GetPowerFlowRealtimeDataResponse {
  sealed trait Mode
  case object ProduceOnly extends Mode
  case object Meter extends Mode
  case object VagueMeter extends Mode
  case object Bidirectional extends Mode
  case object ACCoupled extends Mode

  case class Inverter(
      DT: Double,
      E_Day: Double,
      E_Total: Double,
      E_Year: Double,
      P: Double
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
      Meter_Location: Option[String],
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
