package me.zanini.froniussolar.apiclient

import io.circe.Json

object GetPowerFlowRealtimeDataResponse {
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
      E_Day: Double,
      E_Total: Double,
      E_Year: Double,
      Meter_Location: String,
      Mode: String,
      P_Akku: Double,
      P_Grid: Double,
      P_Load: Double,
      P_PV: Double,
      rel_Autonomy: Json,
      rel_SelfConsumption: Json
  )

  case class Status(
      Code: Int,
      Reason: String,
      UserMessage: String
  )
}
