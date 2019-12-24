package me.zanini.froniussolar

import cats.data.NonEmptyList
import cats.effect.Sync
import org.http4s.Uri
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.FailureReason
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import pureconfig.module.cats._

import scala.concurrent.duration.FiniteDuration

case class AppConfig(sites: NonEmptyList[SiteConfig])
case class SiteConfig(name: String, baseUrl: Uri, pollInterval: FiniteDuration)
    extends Site

trait Site {
  def name: String
  def baseUrl: Uri
}

object AppConfig {
  implicit def uriConfigReader: ConfigReader[Uri] =
    ConfigReader[String].emap(
      Uri
        .fromString(_)
        .left
        .map(error =>
          new FailureReason {
            override def description: String = error.message
        }))

  def readDefault[F[_]: Sync]: F[AppConfig] =
    ConfigSource.default.loadF[F, AppConfig]
}
