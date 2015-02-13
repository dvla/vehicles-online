package models

import uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix

object DisposeCacheKeyPrefix {

  implicit final val CookiePrefix = CacheKeyPrefix("dtt-")

}
