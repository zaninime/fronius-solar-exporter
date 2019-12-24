package me.zanini.froniussolar.metrics

import cats.{Parallel, Show}
import cats.effect.Sync
import me.zanini.froniussolar.Site
import me.zanini.froniussolar.apiclient.GetPowerFlowRealtimeDataResponse
import cats.syntax.parallel._
import cats.syntax.functor._
import cats.syntax.apply._
import cats.instances.list._

trait UpdateService[F[_]] {
  def from(response: GetPowerFlowRealtimeDataResponse.Root): F[Unit]
}

class UpdateServiceImpl[F[_]: Parallel](site: Site)(implicit F: Sync[F])
    extends UpdateService[F] {
  override def from(response: GetPowerFlowRealtimeDataResponse.Root): F[Unit] =
    F.delay {
      SiteMetrics.lastSuccessfulQueryMetric
        .labels(site.name)
        .setToCurrentTime()
    } *> setSiteEnergyPower(response.Body.Data.Site) *> setSiteMode(
      response.Body.Data.Site.Mode)

  private def setSiteEnergyPower(
      siteData: GetPowerFlowRealtimeDataResponse.Site): F[Unit] =
    List(
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "day")
          .set(siteData.E_Day.getOrElse(-1))),
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "year")
          .set(siteData.E_Year.getOrElse(-1))),
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "total")
          .set(siteData.E_Total.getOrElse(-1))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "battery")
          .set(siteData.P_Akku.getOrElse(-1))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "grid")
          .set(siteData.P_Grid.getOrElse(-1))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "load")
          .set(siteData.P_Load.getOrElse(-1))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "pv")
          .set(siteData.P_PV.getOrElse(-1))),
    ).parSequence
      .as(())

  private def setSiteMode(mode: GetPowerFlowRealtimeDataResponse.Mode) = {
    import me.zanini.froniussolar.apiclient.GetPowerFlowRealtimeDataResponse._

    implicit def showMode: Show[Mode] = {
      case GetPowerFlowRealtimeDataResponse.ProduceOnly   => "produce-only"
      case GetPowerFlowRealtimeDataResponse.Meter         => "meter"
      case GetPowerFlowRealtimeDataResponse.VagueMeter    => "vague-meter"
      case GetPowerFlowRealtimeDataResponse.Bidirectional => "bidirectional"
      case GetPowerFlowRealtimeDataResponse.ACCoupled     => "ac-coupled"
    }

    List(ProduceOnly, Meter, VagueMeter, Bidirectional, ACCoupled)
      .map(currentMode =>
        if (currentMode == mode) {
          F.delay(
            SiteMetrics.modeMetric
              .labels(site.name, Show[Mode].show(currentMode))
              .set(1))
        } else {
          F.delay(
            SiteMetrics.modeMetric
              .labels(site.name, Show[Mode].show(currentMode))
              .set(0))
      })
      .parSequence
      .as(())
  }
}
