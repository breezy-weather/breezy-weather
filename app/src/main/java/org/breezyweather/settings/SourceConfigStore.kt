package org.breezyweather.settings

import android.content.Context

class SourceConfigStore(
    context: Context, sourceId: String
) : ConfigStore(context, "source_" + sourceId + "_preferences")