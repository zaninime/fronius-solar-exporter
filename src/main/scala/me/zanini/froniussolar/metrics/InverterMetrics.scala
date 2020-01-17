package me.zanini.froniussolar.metrics

import io.prometheus.client.Gauge
import me.zanini.froniussolar.metrics.Constants.InverterMetricsNamespace

object InverterMetrics {
  private val commonLabels = List("site", "device_id")

  val deviceTypeMetric: Gauge =
    registerMetric("device_type", "Device type", commonLabels)

  val powerMetric: Gauge =
    registerMetric("power_watts", "Current power", commonLabels)

  val chargeMetric: Gauge =
    registerMetric(
      "battery_charge_percent",
      "Current battery charge",
      commonLabels
    )

  val batteryModeMetric: Gauge =
    registerMetric("battery_mode", "Battery mode", commonLabels ++ List("mode"))

  val energyMetric: Gauge =
    registerMetric(
      "energy_watthours",
      "Cumulative energy",
      commonLabels ++ List("period")
    )

  private def registerMetric(name: String,
                             help: String,
                             labelNames: List[String]) =
    Gauge
      .build(name, help)
      .namespace(InverterMetricsNamespace)
      .labelNames(labelNames: _*)
      .register()
}
