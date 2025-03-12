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

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.breezyweather.R
import org.json.JSONObject

/**
 * Test activity for mMCP functionality
 * This activity can:
 * 1. Discover available mMCP tools
 * 2. Call the say_hi tool
 */
class MMCPTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MMCPTestActivity"
    }

    private lateinit var resultTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var discoverButton: Button
    private lateinit var sayHiButton: Button

    private val toolCallLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val toolResult = result.data?.getStringExtra(MMCPActivity.EXTRA_RESULT) ?: "No result"
            resultTextView.text = "Result: $toolResult"
        } else {
            val errorResult = result.data?.getStringExtra(MMCPActivity.EXTRA_RESULT) ?: "Unknown error"
            resultTextView.text = "Error: $errorResult"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmcp_test)

        resultTextView = findViewById(R.id.resultTextView)
        nameEditText = findViewById(R.id.nameEditText)
        discoverButton = findViewById(R.id.discoverButton)
        sayHiButton = findViewById(R.id.sayHiButton)

        discoverButton.setOnClickListener {
            discoverTools()
        }

        sayHiButton.setOnClickListener {
            callSayHiTool()
        }
    }

    /**
     * Discover available mMCP tools
     */
    private fun discoverTools() {
        val queryIntent = Intent(MMCPActivity.ACTION_TOOL_ADVERTISE)
        val activities = packageManager.queryIntentActivities(queryIntent, 0)

        val sb = StringBuilder()
        sb.append("Discovered tools:\n")

        for (info in activities) {
            try {
                val activityInfo = info.activityInfo
                val metaData = activityInfo.metaData
                val manifestJson = metaData?.getString("mMCP_manifest")

                if (manifestJson != null) {
                    val manifest = JSONObject(manifestJson)
                    val instructions = manifest.optString("instructions", "No instructions")
                    val tools = manifest.optJSONArray("tools")

                    sb.append("App: ${activityInfo.applicationInfo.loadLabel(packageManager)}\n")
                    sb.append("Instructions: $instructions\n")
                    sb.append("Tools:\n")

                    if (tools != null) {
                        for (i in 0 until tools.length()) {
                            val tool = tools.getJSONObject(i)
                            val name = tool.optString("name", "unnamed")
                            val description = tool.optString("description", "No description")
                            sb.append("- $name: $description\n")
                        }
                    } else {
                        sb.append("No tools found\n")
                    }
                    sb.append("\n")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing mMCP manifest", e)
                sb.append("Error parsing manifest: ${e.message}\n")
            }
        }

        resultTextView.text = sb.toString()
    }

    /**
     * Call the say_hi tool
     */
    private fun callSayHiTool() {
        val name = nameEditText.text.toString().ifEmpty { "friend" }
        
        val parameters = JSONObject().apply {
            put("name", name)
        }

        val callIntent = Intent(MMCPActivity.ACTION_TOOL_CALL).apply {
            putExtra(MMCPActivity.EXTRA_TOOL_NAME, "say_hi")
            putExtra(MMCPActivity.EXTRA_PARAMETERS, parameters.toString())
        }

        toolCallLauncher.launch(callIntent)
    }
}