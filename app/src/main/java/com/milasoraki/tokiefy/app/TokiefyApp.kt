package com.milasoraki.tokiefy.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

/**
 * Application subclass.
 *
 * Implements [ImageLoaderFactory] to configure Coil once (crossfade +
 * relaxed cache headers) so individual screens do not need to pass an
 * explicit [ImageLoader] around.
 */
public class TokiefyApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(enable = true)
        .respectCacheHeaders(false)
        .build()
}
