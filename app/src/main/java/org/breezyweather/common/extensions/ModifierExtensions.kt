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

package org.breezyweather.common.extensions

import android.view.View
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue

/**
 * Source: ProAndroidDev
 * https://proandroiddev.com/jetpack-compose-tricks-conditionally-applying-modifiers-for-dynamic-uis-e3fe5a119f45
 */
inline fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier = { this },
): Modifier = if (condition) {
    then(ifTrue(Modifier))
} else {
    then(ifFalse(Modifier))
}

// Simplified version of https://stackoverflow.com/a/77321467
fun Modifier.handleNestedHorizontalDragGesture(
    view: View,
) = pointerInput(Unit) {
    var initialX = 0f
    var initialY = 0f

    awaitEachGesture {
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            when (event.type) {
                PointerEventType.Press -> {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    event.changes.firstOrNull()?.let {
                        initialX = it.position.x
                        initialY = it.position.y
                    }
                }
                PointerEventType.Move -> {
                    event.changes.firstOrNull()?.let {
                        val changedX = it.previousPosition.x - initialX
                        val changedY = it.previousPosition.y - initialY

                        if (changedY.absoluteValue > changedX.absoluteValue) {
                            view.parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            view.parent.requestDisallowInterceptTouchEvent(true)
                            it.consume()
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}
