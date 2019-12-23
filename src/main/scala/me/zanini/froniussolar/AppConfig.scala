package me.zanini.froniussolar

import monix.eval.Task
import org.http4s.Uri
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.{ConfigReaderFailures, FailureReason}
import pureconfig.generic.auto._

import scala.concurrent.duration.FiniteDuration

case class AppConfig(sites: List[SiteConfig])
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

  def readDefault = {
    Task.fromEither((err: ConfigReaderFailures) =>
      new Exception(err.prettyPrint()))(ConfigSource.default.load[AppConfig])
  }
}
