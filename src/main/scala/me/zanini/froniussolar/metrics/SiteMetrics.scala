package me.zanini.froniussolar.metrics

import io.prometheus.client.Gauge
import me.zanini.froniussolar.metrics.Constants.SiteMetricsNamespace

object SiteMetrics {
  private val commonLabels = List("site")

  val modeMetric: Gauge =
    registerMetric(
      "mode",
      "0 = not in this mode, 1 = running in this mode",
      commonLabels ++ List("mode")
    )

  val batteryStandbyMetric: Gauge =
    registerMetric("battery_standby", "1 = standby, 0 = off", commonLabels)

  val backupModeMetric: Gauge =
    registerMetric("backup_mode", "1 = active, 0 = inactive", commonLabels)

  val powerMetric: Gauge =
    registerMetric(
      "power_watts",
      "Instant power",
      commonLabels ++ List("device")
    )

  val energyMetric: Gauge =
    registerMetric(
      "energy_watthours",
      "Cumulative energy",
      commonLabels ++ List("period")
    )

  val selfConsumptionPercentMetric: Gauge =
    registerMetric(
      "self_consumption_percent",
      "Current relative self consumption in %",
      commonLabels
    )

  val autonomyPercentMetric: Gauge =
    registerMetric(
      "autonomy_percent",
      "Current relative autonomy in %",
      commonLabels
    )

  val meterLocationMetric: Gauge =
    registerMetric(
      "meter_location",
      "unknown = backup power",
      commonLabels ++ List("location")
    )

  val lastSuccessfulQueryMetric: Gauge =
    registerMetric(
      "last_successful_query",
      "When the site was last successfully queried",
      commonLabels
    )

  private def registerMetric(name: String,
                             help: String,
                             labelNames: List[String]) =
    Gauge
      .build(name, help)
      .namespace(SiteMetricsNamespace)
      .labelNames(labelNames: _*)
      .register()
}
