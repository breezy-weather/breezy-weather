package org.breezyweather.common.source

// TODO: We should inject Retrofit.Builder here, however I still haven’t figure out
// how to do it yet
abstract class HttpSource : Source {

    /**
     * Privacy policy of the website, like: https://mysite.com/privacy
     * TODO: Implement this in UI
     */
    abstract val privacyPolicyUrl: String

}