package org.breezyweather.main.adapters.main

interface ViewType {
    companion object {
        const val REFRESH_TIME = 0
        const val HEADER = 1
        const val DAILY = 2
        const val HOURLY = 3
        const val AIR_QUALITY = 4
        const val ALLERGEN = 5
        const val ASTRO = 6
        const val LIVE = 7
        const val FOOTER = -1
    }
}
