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
      response.Body.Data.Site.Meter_Location) *> setInvertersMetrics(
      response.Body.Data)

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

  private def setInvertersMetrics(data: GetPowerFlowRealtimeDataResponse.Data) =
    data.Inverters.toList
      .map {
        case (deviceId, inverter) => setInverterMetrics(deviceId, inverter)
      }
      .parSequence
      .as(())

  private def setInverterMetrics(
      deviceId: String,
      inverterData: GetPowerFlowRealtimeDataResponse.Inverter) = {
    List(
      F.delay(
        InverterMetrics.deviceTypeMetric
          .labels(site.name, deviceId)
          .set(inverterData.DT.toDouble)),
      F.delay(
        InverterMetrics.powerMetric
          .labels(site.name, deviceId)
          .set(inverterData.P.toDouble)),
      F.delay(
        InverterMetrics.chargeMetric
          .labels(site.name, deviceId)
          .set(inverterData.SOC.fold(Double.NaN)(_.toDouble))),
      F.delay(
        InverterMetrics.energyMetric
          .labels(site.name, deviceId, "day")
          .set(inverterData.E_Day.getOrElse(Double.NaN))),
      F.delay(
        InverterMetrics.energyMetric
          .labels(site.name, deviceId, "year")
          .set(inverterData.E_Year.getOrElse(Double.NaN))),
      F.delay(
        InverterMetrics.energyMetric
          .labels(site.name, deviceId, "total")
          .set(inverterData.E_Total.getOrElse(Double.NaN))),
      setInverterBatteryMode(deviceId, inverterData.Battery_Mode)
    ).parSequence.as(())
  }

  private def setInverterBatteryMode(
      deviceId: String,
      mode: Option[GetPowerFlowRealtimeDataResponse.BatteryMode]) = {
    import GetPowerFlowRealtimeDataResponse._

    implicit def showMode: Show[BatteryMode] = {
      case Disabled                                => "disabled"
      case GetPowerFlowRealtimeDataResponse.Normal => "normal"
      case Service                                 => "service"
      case ChargeBoost                             => "charge-boost"
      case NearlyDepleted                          => "nearly-depleted"
      case Suspended                               => "suspended"
      case Calibrate                               => "calibrate"
      case GridSupport                             => "grid-support"
      case DepleteRecovery                         => "deplete-recovery"
      case NonOperableVoltage                      => "non-operable-voltage"
      case NonOperableTemperature                  => "non-operable-temperature"
      case Preheating                              => "preheating"
      case Startup                                 => "startup"
    }

    List(
      Disabled,
      Normal,
      Service,
      ChargeBoost,
      NearlyDepleted,
      Suspended,
      Calibrate,
      GridSupport,
      DepleteRecovery,
      NonOperableVoltage,
      NonOperableTemperature,
      Preheating,
      Startup
    ).map(currentMode => {
        val isEnabled = mode.fold(false)(thisMode => thisMode == currentMode)
        F.delay(
          InverterMetrics.batteryModeMetric
            .labels(site.name, deviceId, Show[BatteryMode].show(currentMode))
            .set(bool2double(isEnabled)))
      })
      .parSequence
      .as(())
  }

  private def bool2double(v: Boolean): Double = if (v) 1 else 0
}
