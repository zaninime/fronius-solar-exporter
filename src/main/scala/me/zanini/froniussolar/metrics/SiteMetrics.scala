package me.zanini.froniussolar.metrics

import io.prometheus.client.Gauge
import me.zanini.froniussolar.metrics.Constants.METRICS_NS

object SiteMetrics {
  val totalEnergyMetric: Gauge = Gauge
    .build()
    .namespace(METRICS_NS)
    .name("daily_energy_wh")
    .help("Energy produced today")
    .labelNames("site")
    .register()

  val lastSuccessfulQueryMetric: Gauge = Gauge
    .build()
    .namespace(METRICS_NS)
    .name("last_successful_query")
    .help("When the site was last successfully queried")
    .labelNames("site")
    .register()
}
