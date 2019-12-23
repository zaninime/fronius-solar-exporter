package me.zanini.froniussolar.metrics

import cats.effect.Sync
import me.zanini.froniussolar.Site
import me.zanini.froniussolar.apiclient.GetPowerFlowRealtimeDataResponse

trait UpdateService[F[_]] {
  def from(response: GetPowerFlowRealtimeDataResponse.Root): F[Unit]
}

class UpdateServiceImpl[F[_]: Sync](site: Site) extends UpdateService[F] {
  override def from(response: GetPowerFlowRealtimeDataResponse.Root): F[Unit] =
    Sync[F].delay {
      SiteMetrics.totalEnergyMetric
        .labels(site.name)
        .set(response.Body.Data.Site.E_Day)
      SiteMetrics.lastSuccessfulQueryMetric
        .labels(site.name)
        .setToCurrentTime()
    }
}
