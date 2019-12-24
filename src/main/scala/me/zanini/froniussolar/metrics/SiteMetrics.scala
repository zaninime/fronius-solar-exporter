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

  val powerMetric: Gauge = GaugeMetric("power_watts",
                                       "Instant power, -1 when N/A",
                                       commonLabels ++ List("device")).register

  val energyMetric: Gauge = GaugeMetric("energy_watthours",
                                        "Cumulative energy, -1 when N/A",
                                        commonLabels ++ List("period")).register

  val lastSuccessfulQueryMetric: Gauge = GaugeMetric(
    "last_successful_query",
    "When the site was last successfully queried",
    commonLabels).register
}
