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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * Activity that handles mMCP (Mobile Model Context Protocol) intents
 * This activity can:
 * 1. Advertise available tools to AI models
 * 2. Handle tool invocations
 */
class MMCPActivity : Activity() {

    companion object {
        private const val TAG = "MMCPActivity"
        
        // mMCP action constants
        const val ACTION_TOOL_ADVERTISE = "com.example.mMCP.ACTION_TOOL_ADVERTISE"
        const val ACTION_TOOL_CALL = "com.example.mMCP.ACTION_TOOL_CALL"
        
        // Intent extras
        const val EXTRA_TOOL_NAME = "tool_name"
        const val EXTRA_PARAMETERS = "parameters"
        const val EXTRA_RESULT = "result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle the intent
        when (intent?.action) {
            ACTION_TOOL_CALL -> handleToolCall()
            ACTION_TOOL_ADVERTISE -> {
                // Nothing to do for advertisement, the meta-data in the manifest handles this
                finish()
            }
            else -> finish() // Nothing to do for other actions, activity is just a receiver
        }
    }
    
    /**
     * Handle a tool call intent
     */
    private fun handleToolCall() {
        val toolName = intent.getStringExtra(EXTRA_TOOL_NAME)
        val parametersJson = intent.getStringExtra(EXTRA_PARAMETERS)
        
        if (toolName.isNullOrEmpty() || parametersJson.isNullOrEmpty()) {
            setErrorResult("Missing tool name or parameters")
            return
        }
        
        try {
            val parameters = JSONObject(parametersJson)
            
            // Handle different tools
            val result = when (toolName) {
                "say_hi" -> handleSayHi(parameters)
                else -> "Unknown tool: $toolName"
            }
            
            // Return the result
            setSuccessResult(result)
            
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing parameters JSON", e)
            setErrorResult("Invalid parameters JSON: ${e.message}")
        }
    }
    
    /**
     * Handle the say_hi tool
     */
    private fun handleSayHi(parameters: JSONObject): String {
        val name = parameters.optString("name", "friend")
        return "Hello, $name! Nice to meet you from Breezy Weather!"
    }
    
    /**
     * Set a success result for the activity
     */
    private fun setSuccessResult(result: String) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_RESULT, result)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    /**
     * Set an error result for the activity
     */
    private fun setErrorResult(errorMessage: String) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_RESULT, "Error: $errorMessage")
        }
        setResult(RESULT_CANCELED, resultIntent)
        finish()
    }
}