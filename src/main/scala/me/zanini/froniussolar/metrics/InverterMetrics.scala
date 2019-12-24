package me.zanini.froniussolar.metrics

import io.prometheus.client.Gauge
import me.zanini.froniussolar.metrics.Constants.INVERTER_METRICS_NS

object InverterMetrics {
  private val commonLabels = List("site", "device_id")

  private case class GaugeMetric(name: String,
                                 help: String,
                                 labelNames: List[String]) {
    def register: Gauge =
      Gauge
        .build(name, help)
        .namespace(INVERTER_METRICS_NS)
        .labelNames(labelNames: _*)
        .register()
  }

  def deviceTypeMetric: Gauge =
    GaugeMetric("device_type", "Device type", commonLabels).register

  def powerMetric: Gauge =
    GaugeMetric("power_watts", "Current power", commonLabels).register

  def chargeMetric: Gauge =
    GaugeMetric("battery_charge_percent",
                "Current battery charge",
                commonLabels).register

  def batteryModeMetric: Gauge =
    GaugeMetric("battery_mode", "Battery mode", commonLabels ++ List("mode")).register

  def energyMetric: Gauge =
    GaugeMetric("energy_watthours",
                "Cumulative energy",
                commonLabels ++ List("period")).register
}
