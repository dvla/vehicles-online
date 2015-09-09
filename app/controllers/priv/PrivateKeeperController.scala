package controllers.priv

import controllers.BusinessController

trait PrivateKeeperController extends BusinessController {

  override protected implicit val isPrivateKeeper = true

  override protected implicit val AllCacheKeys = models.PrivateAllCacheKeys
  override protected implicit val DisposeCacheKeys = models.PrivateDisposeCacheKeys
  override protected implicit val DisposeOnlyCacheKeys = models.PrivateDisposeOnlyCacheKeys
}