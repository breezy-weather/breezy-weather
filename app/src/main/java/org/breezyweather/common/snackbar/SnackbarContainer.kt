package org.breezyweather.common.snackbar

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner

class SnackbarContainer(
    val owner: LifecycleOwner?,
    val container: ViewGroup,
    val cardStyle: Boolean
)
