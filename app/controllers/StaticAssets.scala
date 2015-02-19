package controllers

import controllers.Assets.Asset

/**
 * Allows to serve assets from another domain like a CDN. if maybeAssetsUrl is set then this will
 * be prepended to the asset path, otherwise the original path is returned.
 *
 * maybeAssetsUrl should come from a configuration option.
 */
object StaticAssets {
  
  def versioned(maybeAssetsUrl: Option[String])(file: Asset): String = {
    
    maybeAssetsUrl.map(_ + routes.Assets.versioned(file).url).getOrElse( routes.Assets.versioned(file).url)

  }
}
