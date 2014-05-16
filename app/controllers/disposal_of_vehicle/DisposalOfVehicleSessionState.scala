package controllers.disposal_of_vehicle

import play.api.libs.json.{Writes, Reads, JsPath, Json}
import play.api.mvc._
import utils.helpers.{CookieNameHashing, CookieEncryption, CryptoHelper}
import play.api.data.Form
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import mappings.disposal_of_vehicle.SetupTradeDetails._
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import play.api.mvc.DiscardingCookie
import mappings.disposal_of_vehicle.TraderDetails._
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import play.api.mvc.DiscardingCookie
import mappings.disposal_of_vehicle.BusinessChooseYourAddress._
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import play.api.mvc.DiscardingCookie
import mappings.disposal_of_vehicle.VehicleLookup._
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import play.api.mvc.DiscardingCookie
import mappings.disposal_of_vehicle.Dispose._
import models.domain.common.CacheKey
import scala.Some
import play.api.data.validation.ValidationError
import play.api.mvc.SimpleResult
import play.api.mvc.DiscardingCookie

case class JsonValidationException(errors: Seq[(JsPath, Seq[ValidationError])]) extends Exception

object DisposalOfVehicleSessionState {

  implicit class RequestAdapter[A](val request: Request[A]) extends AnyVal {
    def getEncryptedCookie[B](implicit fjs: Reads[B], cacheKey: CacheKey[B], encryption: CookieEncryption, cookieNameHashing: CookieNameHashing): Option[B] = {
      val salt = CryptoHelper.getSessionSecretKeyFromRequest(request).getOrElse("")
      request.cookies.get(cookieNameHashing.hash(salt + cacheKey.value)).map { cookie =>
        val decrypted = encryption.decrypt(cookie.value)
        val parsed = Json.parse(decrypted)
        val fromJson = Json.fromJson[B](parsed)
        fromJson.asEither match {
          case Left(errors) => throw JsonValidationException(errors)
          case Right(model) => model
        }
      }
    }

    def getCookieNamed(key: String)(implicit encryption: CookieEncryption, cookieNameHashing: CookieNameHashing): Option[String] = {
      val salt = CryptoHelper.getSessionSecretKeyFromRequest(request).getOrElse("")
      request.cookies.get(cookieNameHashing.hash(salt + key)).map { cookie =>
        encryption.decrypt(cookie.value)
      }
    }
  }

  implicit class SimpleResultAdapter(val inner: SimpleResult) extends AnyVal {

    def withEncryptedCookie[A](model: A)(implicit tjs: Writes[A], cacheKey: CacheKey[A], request: Request[_],
                                encryption: CookieEncryption, cookieNameHashing: CookieNameHashing): SimpleResult = {

      def withModel(resultWithSalt: (SimpleResult, String)): SimpleResult = {
        val (result, salt) = resultWithSalt
        val stateAsJson = Json.toJson(model)
        val encryptedStateAsJson = encryption.encrypt(stateAsJson.toString())
        val cookie = CryptoHelper.createCookie(name = cookieNameHashing.hash(salt + cacheKey.value),
          value = encryptedStateAsJson)

        result.withCookies(cookie)
      }

      Some(inner)
        .map(CryptoHelper.ensureSessionSecretKeyInResult)
        .map(withModel)
        .get
    }

    def discardingEncryptedCookies(keys: Seq[String])(implicit request: Request[_], encryption: CookieEncryption,
                                                      cookieNameHashing: CookieNameHashing): SimpleResult = {
      val salt = CryptoHelper.getSessionSecretKeyFromRequest(request).getOrElse("")
      val cookiesToDiscard = keys.map(cookieName => DiscardingCookie(name = cookieNameHashing.hash(salt + cookieName)))
      inner.discardingCookies(cookiesToDiscard: _*)
    }

    def withEncryptedCookie(key: String, value: String)(implicit request: Request[_], encryption: CookieEncryption,
                                               cookieNameHashing: CookieNameHashing): SimpleResult = {

      def withKeyValuePair(resultWithSalt: (SimpleResult, String)): SimpleResult = {
        val (result, salt) = resultWithSalt
        val encrypted = encryption.encrypt(value)
        val cookie = CryptoHelper.createCookie(name = cookieNameHashing.hash(salt + key),
          value = encrypted)
        result.withCookies(cookie)
      }

      Some(inner)
        .map(CryptoHelper.ensureSessionSecretKeyInResult)
        .map(withKeyValuePair)
        .get
    }
  }

  implicit class FormAdapter[A](val f: Form[A]) extends AnyVal {
    def fill()(implicit request: Request[_], fjs: Reads[A], cacheKey: CacheKey[A], encryption: CookieEncryption, hashing: CookieNameHashing): Form[A] =
      request.getEncryptedCookie[A] match {
        case Some(v) => f.fill(v) // Found cookie so fill the form with the cached data.
        case _ => f // No cookie found so return a blank form.
      }
  }


}
