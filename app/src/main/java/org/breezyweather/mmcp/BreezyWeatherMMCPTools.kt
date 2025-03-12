/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.mmcp

import org.json.JSONObject

/**
 * Defines the tools that Breezy Weather exposes via mMCP (Mobile Model Context Protocol)
 */
object BreezyWeatherMMCPTools {
    
    /**
     * Get the instructions for the AI model about Breezy Weather's capabilities
     */
    fun getInstructions(): String {
        return "Breezy Weather is a weather application that provides weather forecasts, " +
                "current conditions, and other weather-related information. " +
                "You can use these tools to interact with Breezy Weather."
    }
    
    /**
     * Get the list of tools that Breezy Weather exposes
     */
    fun getTools(): List<JSONObject> {
        return listOf(
            getSayHiTool()
        )
    }
    
    /**
     * A simple tool that responds with a greeting
     */
    private fun getSayHiTool(): JSONObject {
        val parameters = JSONObject().apply {
            put("type", "object")
            put("required", arrayOf("name"))
            put("properties", JSONObject().apply {
                put("name", JSONObject().apply {
                    put("type", "string")
                    put("description", "The name of the person to greet")
                })
            })
        }
        
        return JSONObject().apply {
            put("name", "say_hi")
            put("description", "A simple tool that responds with a greeting message")
            put("parameters", parameters)
        }
    }
}