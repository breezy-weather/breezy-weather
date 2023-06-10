package wangdaye.com.geometricweather.db

import android.content.Context
import io.objectbox.BoxStore
import io.objectbox.android.Admin
import wangdaye.com.geometricweather.BuildConfig
import wangdaye.com.geometricweather.common.utils.helpers.LogHelper
import wangdaye.com.geometricweather.db.entities.MyObjectBox

/**
 * Singleton to keep BoxStore reference.
 */
object ObjectBox {

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()

        if (BuildConfig.DEBUG) {
            LogHelper.log("Using ObjectBox ${BoxStore.getVersion()} (${BoxStore.getVersionNative()})")
            Admin(boxStore).start(context.applicationContext)
        }
    }

}