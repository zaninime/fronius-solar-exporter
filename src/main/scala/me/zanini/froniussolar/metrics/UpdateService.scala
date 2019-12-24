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
      response.Body.Data.Site.Mode) *> setMeterLocation(
      response.Body.Data.Site.Meter_Location)

  private def setSiteEnergyPower(
      siteData: GetPowerFlowRealtimeDataResponse.Site): F[Unit] =
    List(
      F.delay(
        SiteMetrics.backupModeMetric
          .labels(site.name)
          .set(siteData.BackupMode.fold(Double.NaN)(bool2double))),
      F.delay(
        SiteMetrics.batteryStandbyMetric
          .labels(site.name)
          .set(siteData.BatteryStandby.fold(Double.NaN)(bool2double))),
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "day")
          .set(siteData.E_Day.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "year")
          .set(siteData.E_Year.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.energyMetric
          .labels(site.name, "total")
          .set(siteData.E_Total.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "battery")
          .set(siteData.P_Akku.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "grid")
          .set(siteData.P_Grid.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "load")
          .set(siteData.P_Load.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.powerMetric
          .labels(site.name, "pv")
          .set(siteData.P_PV.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.selfConsumptionPercentMetric
          .labels(site.name)
          .set(siteData.rel_SelfConsumption.getOrElse(Double.NaN))),
      F.delay(
        SiteMetrics.autonomyPercentMetric
          .labels(site.name)
          .set(siteData.rel_Autonomy.getOrElse(Double.NaN))),
    ).parSequence
      .as(())

  private def setSiteMode(mode: GetPowerFlowRealtimeDataResponse.Mode) = {
    import me.zanini.froniussolar.apiclient.GetPowerFlowRealtimeDataResponse._

    implicit def showMode: Show[Mode] = {
      case ProduceOnly   => "produce-only"
      case Meter         => "meter"
      case VagueMeter    => "vague-meter"
      case Bidirectional => "bidirectional"
      case ACCoupled     => "ac-coupled"
    }

    List(ProduceOnly, Meter, VagueMeter, Bidirectional, ACCoupled)
      .map(
        currentMode =>
          F.delay(
            SiteMetrics.modeMetric
              .labels(site.name, Show[Mode].show(currentMode))
              .set(bool2double(currentMode == mode))))
      .parSequence
      .as(())
  }

  private def setMeterLocation(
      location: Option[GetPowerFlowRealtimeDataResponse.MeterLocation]) = {
    import me.zanini.froniussolar.apiclient.GetPowerFlowRealtimeDataResponse._

    implicit def showMeterLocation: Show[MeterLocation] = {
      case Load    => "load"
      case Grid    => "grid"
      case Unknown => "unknown"
    }

    List(Load, Grid, Unknown)
      .map(currentLocation => {
        val isEnabled =
          location.fold(false)(thisLocation => currentLocation == thisLocation)
        F.delay(
          SiteMetrics.meterLocationMetric
            .labels(site.name, Show[MeterLocation].show(currentLocation))
            .set(bool2double(isEnabled)))
      })
      .parSequence
      .as(())
  }

  private def bool2double(v: Boolean): Double = if (v) 1 else 0
}
