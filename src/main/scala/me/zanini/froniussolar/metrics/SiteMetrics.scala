package me.zanini.froniussolar.metrics

import io.prometheus.client.Gauge
import me.zanini.froniussolar.metrics.Constants.SITE_METRICS_NS

object SiteMetrics {
  private val commonLabels = List("site")

  private case class GaugeMetric(name: String,
                                 help: String,
                                 labelNames: List[String]) {
    def register: Gauge =
      Gauge
        .build(name, help)
        .namespace(SITE_METRICS_NS)
        .labelNames(labelNames: _*)
        .register()
  }
  val modeMetric: Gauge = GaugeMetric(
    "mode",
    "0 = not in this mode, 1 = running in this mode",
    commonLabels ++ List("mode")).register

  val batteryStandbyMetric: Gauge =
    GaugeMetric("battery_standby", "1 = standby, 0 = off", commonLabels).register

  val backupModeMetric: Gauge =
    GaugeMetric("backup_mode", "1 = active, 0 = inactive", commonLabels).register

  val powerMetric: Gauge = GaugeMetric("power_watts",
                                       "Instant power",
                                       commonLabels ++ List("device")).register

  val energyMetric: Gauge = GaugeMetric("energy_watthours",
                                        "Cumulative energy",
                                        commonLabels ++ List("period")).register

  val selfConsumptionPercentMetric: Gauge = GaugeMetric(
    "self_consumption_percent",
    "Current relative self consumption in %",
    commonLabels).register

  val autonomyPercentMetric: Gauge = GaugeMetric(
    "autonomy_percent",
    "Current relative autonomy in %",
    commonLabels).register

  val meterLocationMetric: Gauge =
    GaugeMetric("meter_location",
                "unknown = backup power",
                commonLabels ++ List("location")).register

  val lastSuccessfulQueryMetric: Gauge = GaugeMetric(
    "last_successful_query",
    "When the site was last successfully queried",
    commonLabels).register
}
