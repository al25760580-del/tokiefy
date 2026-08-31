package com.milasoraki.tokiefy.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.SessionManager

/**
 * Application subclass.
 *
 * Owns long-lived singletons that need a [Context]: Coil configuration
 * and the [SessionManager] (built on DataStore). ServiceLocator is
 * initialised from here so repositories can share the same API client.
 */
public class TokiefyApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        val sessionManager = SessionManager(this)
        ServiceLocator.init(applicationContext, sessionManager)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(enable = true)
        .respectCacheHeaders(false)
        .build()
}
