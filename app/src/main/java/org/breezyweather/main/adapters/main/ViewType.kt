package org.breezyweather.main.adapters.main

interface ViewType {
    companion object {
        const val HEADER = 0
        const val DAILY = 1
        const val HOURLY = 2
        const val AIR_QUALITY = 3
        const val ALLERGEN = 4
        const val ASTRO = 5
        const val LIVE = 6
        const val FOOTER = -1
    }
}
