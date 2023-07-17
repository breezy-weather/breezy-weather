package org.breezyweather.common.source

interface Source {
    /**
     * Id for the source. Must be unique.
     */
    val id: String

    /**
     * Name of the source.
     */
    val name: String
}