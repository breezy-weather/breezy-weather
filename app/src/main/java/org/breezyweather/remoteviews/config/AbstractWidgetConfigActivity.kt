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

package org.breezyweather.remoteviews.config

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.extensions.isChinese
import org.breezyweather.common.extensions.launchUI
import org.breezyweather.common.ui.widgets.insets.FitSystemBarNestedScrollView
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.remoteviews.presenters.ClockDayDetailsWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayVerticalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DailyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWidgetIMP
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP
import org.breezyweather.remoteviews.presenters.TextWidgetIMP
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP
import org.breezyweather.settings.ConfigStore
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Abstract widget config activity.
 */
abstract class AbstractWidgetConfigActivity : GeoActivity() {
    protected var mTopContainer: FrameLayout? = null
    protected var mWallpaper: ImageView? = null
    protected var mWidgetContainer: FrameLayout? = null
    protected var mScrollView: NestedScrollView? = null
    protected var mViewTypeContainer: RelativeLayout? = null
    protected var mCardStyleContainer: RelativeLayout? = null
    protected var mCardAlphaContainer: RelativeLayout? = null
    protected var mHideSubtitleContainer: RelativeLayout? = null
    protected var mHideSubtitleTitle: TextView? = null
    protected var mSubtitleDataContainer: RelativeLayout? = null
    protected var mTextColorContainer: RelativeLayout? = null
    protected var mTextSizeContainer: RelativeLayout? = null
    protected var mClockFontContainer: RelativeLayout? = null
    protected var mHideLunarContainer: RelativeLayout? = null
    protected var mAlignEndContainer: RelativeLayout? = null
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var mBottomSheetScrollView: FitSystemBarNestedScrollView? = null
    private var mSubtitleInputLayout: TextInputLayout? = null
    private var mSubtitleEditText: TextInputEditText? = null

    protected var destroyed = false
    protected var viewTypeValueNow: String? = null
    protected var viewTypes: Array<String> = emptyArray()
    protected var viewTypeValues: Array<String> = emptyArray()
    protected var cardStyleValueNow: String? = null
    protected var cardStyles: Array<String> = emptyArray()
    protected var cardStyleValues: Array<String> = emptyArray()
    protected var cardAlpha = 0
    protected var hideSubtitle = false
    protected var subtitleDataValueNow: String? = null
    protected var subtitleData: Array<String> = emptyArray()
    protected var subtitleDataValues: Array<String> = emptyArray()
    protected var textColorValueNow: String? = null
    protected var textColors: Array<String> = emptyArray()
    protected var textColorValues: Array<String> = emptyArray()
    protected var textSize = 0
    protected var clockFontValueNow: String? = null
    protected var clockFonts: Array<String> = emptyArray()
    protected var clockFontValues: Array<String> = emptyArray()
    protected var hideLunar = false
    protected var alignEnd = false
    private var mLastBackPressedTime: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)
        lifecycleScope.launchUI {
            initLocations()
            initData()
            readConfig()
            initView()
            updateHostView()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            setBottomSheetState(true)
            return
        }
        val time = System.currentTimeMillis()
        if (time - mLastBackPressedTime < 2000) {
            super.onBackPressed()
            return
        }
        mLastBackPressedTime = time
        SnackbarHelper.showSnackbar(getString(R.string.message_tap_again_to_exit))
    }

    override fun onCreateView(
        parent: View?, name: String, context: Context,
        attrs: AttributeSet
    ): View? {
        return if (name == "ImageView") {
            ImageView(context, attrs)
        } else super.onCreateView(parent, name, context, attrs)
    }

    override fun onCreateView(
        name: String, context: Context,
        attrs: AttributeSet
    ): View? {
        return if (name == "ImageView") {
            ImageView(context, attrs)
        } else super.onCreateView(name, context, attrs)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        // do nothing.
    }

    abstract suspend fun initLocations()

    @CallSuper
    open fun initData() {
        val res = resources
        viewTypeValueNow = "rectangle"
        viewTypes = res.getStringArray(R.array.widget_styles)
        viewTypeValues = res.getStringArray(R.array.widget_style_values)
        cardStyleValueNow = "none"
        cardStyles = res.getStringArray(R.array.widget_card_styles)
        cardStyleValues = res.getStringArray(R.array.widget_card_style_values)
        cardAlpha = 100
        hideSubtitle = false
        subtitleDataValueNow = "time"
        val data = res.getStringArray(R.array.widget_subtitle_data)
        val dataValues = res.getStringArray(R.array.widget_subtitle_data_values)
        if (this.currentLocale.isChinese) {
            subtitleData = arrayOf(
                data[0], data[1], data[2], data[3], data[4], data[5]
            )
            subtitleDataValues = arrayOf(
                dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[4], dataValues[5]
            )
        } else {
            subtitleData = arrayOf(
                data[0], data[1], data[2], data[3], data[5]
            )
            subtitleDataValues = arrayOf(
                dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[5]
            )
        }
        textColorValueNow = "light"
        textColors = res.getStringArray(R.array.widget_text_colors)
        textColorValues = res.getStringArray(R.array.widget_text_color_values)
        textSize = 100
        clockFontValueNow = "light"
        clockFonts = res.getStringArray(R.array.widget_clock_fonts)
        clockFontValues = res.getStringArray(R.array.widget_clock_font_values)
        hideLunar = false
        alignEnd = false
    }

    private fun readConfig() {
        val config = ConfigStore(this, configStoreName!!)
        viewTypeValueNow = config.getString(getString(R.string.key_view_type), viewTypeValueNow)
        cardStyleValueNow = config.getString(getString(R.string.key_card_style), cardStyleValueNow)
        cardAlpha = config.getInt(getString(R.string.key_card_alpha), cardAlpha)
        hideSubtitle = config.getBoolean(getString(R.string.key_hide_subtitle), hideSubtitle)
        subtitleDataValueNow = config.getString(getString(R.string.key_subtitle_data), subtitleDataValueNow)
        textColorValueNow = config.getString(getString(R.string.key_text_color), textColorValueNow)
        textSize = config.getInt(getString(R.string.key_text_size), textSize)
        clockFontValueNow = config.getString(getString(R.string.key_clock_font), clockFontValueNow)
        hideLunar = config.getBoolean(getString(R.string.key_hide_lunar), hideLunar)
        alignEnd = config.getBoolean(getString(R.string.key_align_end), alignEnd)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @CallSuper
    open fun initView() {
        mWallpaper = findViewById(R.id.activity_widget_config_wall)
        bindWallpaper(true)
        mWidgetContainer = findViewById(R.id.activity_widget_config_widgetContainer)
        val screenWidth = resources.displayMetrics.widthPixels
        val adaptiveWidth = this.getTabletListAdaptiveWidth(screenWidth)
        val paddingHorizontal = (screenWidth - adaptiveWidth) / 2
        mTopContainer = findViewById<FrameLayout>(R.id.activity_widget_config_top).apply {
            setOnApplyWindowInsetsListener { _: View?, insets: WindowInsets ->
                mWidgetContainer!!.setPadding(
                    paddingHorizontal, insets.systemWindowInsetTop,
                    paddingHorizontal, 0
                )
                insets
            }
        }
        mScrollView = findViewById(R.id.activity_widget_config_scrollView)

        mViewTypeContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_viewStyleContainer).apply {
            visibility = View.GONE
        }
        findViewById<AppCompatSpinner>(R.id.activity_widget_config_styleSpinner).also {
            it.onItemSelectedListener = ViewTypeSpinnerSelectedListener()
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, viewTypes)
            it.setSelection(indexValue(viewTypeValues, viewTypeValueNow), true)
        }

        mCardStyleContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_showCardContainer).apply {
            visibility = View.GONE
        }
        findViewById<AppCompatSpinner>(R.id.activity_widget_config_showCardSpinner).also {
            it.onItemSelectedListener = CardStyleSpinnerSelectedListener()
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cardStyles)
            it.setSelection(indexValue(cardStyleValues, cardStyleValueNow), true)
        }

        mCardAlphaContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_cardAlphaContainer).apply {
            visibility = View.GONE
        }
        findViewById<Slider>(R.id.activity_widget_config_cardAlphaSlider).apply {
            valueFrom = 0f
            stepSize = 10f
            valueTo = 100f
            value = ((cardAlpha.toDouble() / 10.0).roundToInt() * 10.0).toFloat()
            setLabelFormatter { value: Float ->
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(value.div(100.0))
            }
            addOnChangeListener { _, value, _ ->
                if (cardAlpha != value.roundToInt()) {
                    cardAlpha = value.roundToInt()
                    updateHostView()
                }
            }
        }

        mHideSubtitleTitle = findViewById<TextView>(R.id.activity_widget_config_hideSubtitleTitle).apply {
            text = getString(R.string.widget_label_hide_subtitle)
        }
        mHideSubtitleContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_hideSubtitleContainer).apply {
            visibility = View.GONE
        }
        findViewById<Switch>(R.id.activity_widget_config_hideSubtitleSwitch).apply {
            setOnCheckedChangeListener(HideSubtitleSwitchCheckListener())
            isChecked = hideSubtitle
        }

        mSubtitleDataContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_subtitleDataContainer).apply {
            visibility = View.GONE
        }
        findViewById<AppCompatSpinner>(R.id.activity_widget_config_subtitleDataSpinner).also {
            it.onItemSelectedListener = SubtitleDataSpinnerSelectedListener()
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, subtitleData)
            it.setSelection(
                indexValue(subtitleDataValues, if (isCustomSubtitle) "custom" else subtitleDataValueNow),
                true
            )
        }

        mTextColorContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_blackTextContainer).apply {
            visibility = View.GONE
        }
        findViewById<AppCompatSpinner>(R.id.activity_widget_config_blackTextSpinner).also {
            it.onItemSelectedListener = TextColorSpinnerSelectedListener()
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, textColors)
            it.setSelection(indexValue(textColorValues, textColorValueNow), true)
        }

        mTextSizeContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_textSizeContainer).apply {
            visibility = View.GONE
        }
        findViewById<Slider>(R.id.activity_widget_config_textSizeSlider).apply {
            valueFrom = 50f
            stepSize = 10f
            valueTo = 250f
            value = max(
                50f,
                min(
                    250f,
                    ((((textSize - 50).toDouble() / 10.0).roundToInt() * 10.0) + 50.0).toFloat()
                )
            )
            addOnChangeListener { _, value, _ ->
                if (textSize != value.roundToInt()) {
                    textSize = value.roundToInt()
                    updateHostView()
                }
            }
            setLabelFormatter { value: Float ->
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(value.div(100.0))
            }
        }

        mClockFontContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_clockFontContainer).apply {
            visibility = View.GONE
        }
        findViewById<AppCompatSpinner>(R.id.activity_widget_config_clockFontSpinner).also {
            it.onItemSelectedListener = ClockFontSpinnerSelectedListener()
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clockFonts)
            it.setSelection(indexValue(clockFontValues, clockFontValueNow), true)
        }

        mHideLunarContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_hideLunarContainer).apply {
            visibility = View.GONE
        }
        findViewById<Switch>(R.id.activity_widget_config_hideLunarSwitch).apply {
            setOnCheckedChangeListener(HideLunarSwitchCheckListener())
            isChecked = hideLunar
        }

        mAlignEndContainer = findViewById<RelativeLayout>(R.id.activity_widget_config_alignEndContainer).apply {
            visibility = View.GONE
        }
        findViewById<Switch>(R.id.activity_widget_config_alignEndSwitch).apply {
            setOnCheckedChangeListener(AlignEndSwitchCheckListener())
            isChecked = alignEnd
        }

        val doneButton = findViewById<Button>(R.id.activity_widget_config_doneButton)
        doneButton.setOnClickListener {
            ConfigStore(this, configStoreName!!)
                .edit()
                .putString(getString(R.string.key_view_type), viewTypeValueNow)
                .putString(getString(R.string.key_card_style), cardStyleValueNow)
                .putInt(getString(R.string.key_card_alpha), cardAlpha)
                .putBoolean(getString(R.string.key_hide_subtitle), hideSubtitle)
                .putString(getString(R.string.key_subtitle_data), subtitleDataValueNow)
                .putString(getString(R.string.key_text_color), textColorValueNow)
                .putInt(getString(R.string.key_text_size), textSize)
                .putString(getString(R.string.key_clock_font), clockFontValueNow)
                .putBoolean(getString(R.string.key_hide_lunar), hideLunar)
                .putBoolean(getString(R.string.key_align_end), alignEnd)
                .apply()
            val intent = intent
            val extras = intent.extras
            var appWidgetId = 0
            if (extras != null) {
                appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
            }
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            when (this) { // TODO: Dirty
                is DayWidgetConfigActivity -> DayWidgetIMP.updateWidgetView(this, locationNow)
                is WeekWidgetConfigActivity -> WeekWidgetIMP.updateWidgetView(this, locationNow)
                is DayWeekWidgetConfigActivity -> DayWeekWidgetIMP.updateWidgetView(this, locationNow)
                is ClockDayHorizontalWidgetConfigActivity -> ClockDayHorizontalWidgetIMP.updateWidgetView(this, locationNow)
                is ClockDayVerticalWidgetConfigActivity -> ClockDayVerticalWidgetIMP.updateWidgetView(this, locationNow)
                is ClockDayWeekWidgetConfigActivity -> ClockDayWeekWidgetIMP.updateWidgetView(this, locationNow)
                is ClockDayDetailsWidgetConfigActivity -> ClockDayDetailsWidgetIMP.updateWidgetView(this, locationNow)
                is TextWidgetConfigActivity -> TextWidgetIMP.updateWidgetView(this, locationNow)
                is DailyTrendWidgetConfigActivity -> DailyTrendWidgetIMP.updateWidgetView(this, locationNow)
                is HourlyTrendWidgetConfigActivity -> HourlyTrendWidgetIMP.updateWidgetView(this, locationNow)
                is MultiCityWidgetConfigActivity -> MultiCityWidgetIMP.updateWidgetView(this, locationList)
            }
            finish()
        }
        mBottomSheetScrollView = findViewById(R.id.activity_widget_config_custom_scrollView)
        mSubtitleInputLayout = findViewById(R.id.activity_widget_config_subtitle_inputLayout)
        mSubtitleEditText = findViewById<TextInputEditText>(R.id.activity_widget_config_subtitle_inputter).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    // do nothing.
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    // do nothing.
                }

                override fun afterTextChanged(editable: Editable) {
                    subtitleDataValueNow = editable.toString()
                    updateHostView()
                }
            })
            setText(if (isCustomSubtitle) subtitleDataValueNow else "")
        }
        val subtitleCustomKeywords = findViewById<TextView>(R.id.activity_widget_config_custom_subtitle_keywords)
        subtitleCustomKeywords.text = this.subtitleCustomKeywords
        val scrollContainer = findViewById<LinearLayout>(R.id.activity_widget_config_scrollContainer)
        scrollContainer.post {
            scrollContainer.setPaddingRelative(
                0, 0, 0, mSubtitleInputLayout!!.measuredHeight
            )
        }
        val bottomSheet = findViewById<AppBarLayout>(R.id.activity_widget_config_custom_subtitle)
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            setState(BottomSheetBehavior.STATE_HIDDEN)
        }
        bottomSheet.post {
            mBottomSheetBehavior!!.peekHeight = mSubtitleInputLayout!!.measuredHeight + mBottomSheetScrollView!!.bottomWindowInset
            setBottomSheetState(isCustomSubtitle)
        }
    }

    fun updateHostView() {
        mWidgetContainer?.let {
            it.removeAllViews()
            it.addView(
                remoteViews.apply(applicationContext, mWidgetContainer),
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun setBottomSheetState(visible: Boolean) {
        if (visible) {
            mBottomSheetBehavior?.isHideable = false
            mBottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            mBottomSheetBehavior?.isHideable = true
            mBottomSheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    abstract val remoteViews: RemoteViews
    abstract val configStoreName: String?
    private fun indexValue(values: Array<String>, current: String?): Int {
        for (i in values.indices) {
            if (values[i] == current) {
                return i
            }
        }
        return 0
    }

    private val isCustomSubtitle: Boolean
        get() {
            for (v in subtitleDataValues) {
                if (v != "custom" && v == subtitleDataValueNow) {
                    return false
                }
            }
            return true
        }
    private val subtitleCustomKeywords: String
        get() = """
            ${getString(R.string.widget_custom_subtitle_keyword_cw)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cw_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_ct)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_ct_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_ctd)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_ctd_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_at)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_at_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_atd)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_atd_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_cwd)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cwd_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_caqi)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_caqi_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_cuv)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cuv_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_ch)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_ch_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_cdp)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cdp_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_cps)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cps_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_cv)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_cv_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_al)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_al_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_als)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_als_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_l)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_l_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_lat)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_lat_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_lon)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_lon_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_ut)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_ut_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_d)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_d_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_lc)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_lc_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_w)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_w_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_ws)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_ws_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_dd)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_dd_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_hd)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_hd_description)}
            ${getString(R.string.widget_custom_subtitle_keyword_enter)}${getString(R.string.colon_separator)}${getString(R.string.widget_custom_subtitle_keyword_enter_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xdw)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xdw_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xnw)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xnw_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xdt)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xdt_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xnt)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xnt_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xdtd)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xdtd_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xntd)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xntd_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xdp)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xdp_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xnp)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xnp_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xdwd)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xdwd_description)}

            ${getString(R.string.widget_custom_subtitle_keyword_xnwd)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xnwd_description)}

            ${getString(R.string.widget_custom_subtitle_keyword_xaqi)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xaqi_description)}

            ${getString(R.string.widget_custom_subtitle_keyword_xsr)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xsr_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xss)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xss_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xmr)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xmr_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xms)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xms_description)}
            
            ${getString(R.string.widget_custom_subtitle_keyword_xmp)}${getString(R.string.colon_separator)}
            ${getString(R.string.widget_custom_subtitle_keyword_xmp_description)}
            """.trimIndent()
    protected val isHideLunarContainerVisible: Int
        get() = if (this.currentLocale.isChinese) View.VISIBLE else View.GONE

    @SuppressLint("MissingPermission")
    private fun bindWallpaper(checkPermissions: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissions) {
            val hasPermission = checkPermissions(0)
            if (!hasPermission) {
                return
            }
        }
        try {
            WallpaperManager.getInstance(this)?.drawable?.let {
                mWallpaper?.setImageDrawable(it)
            }
        } catch (ignore: Exception) {
            // do nothing.
        }
    }

    /**
     * @return true : already got permissions.
     * false: request permissions.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermissions(requestCode: Int): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                bindWallpaper(false)
                if (textColorValueNow == "auto") {
                    updateHostView()
                }
            }

            1 -> {
                bindWallpaper(false)
                updateHostView()
            }
        }
    }

    // on check changed listener(switch).
    private inner class HideSubtitleSwitchCheckListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            hideSubtitle = isChecked
            updateHostView()
        }
    }

    private inner class HideLunarSwitchCheckListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            hideLunar = isChecked
            updateHostView()
        }
    }

    private inner class AlignEndSwitchCheckListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            alignEnd = isChecked
            updateHostView()
        }
    }

    // on item selected listener.
    private inner class ViewTypeSpinnerSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            if (viewTypeValueNow != viewTypeValues[i]) {
                viewTypeValueNow = viewTypeValues[i]
                updateHostView()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            // do nothing.
        }
    }

    private inner class CardStyleSpinnerSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            if (cardStyleValueNow != cardStyleValues[i]) {
                cardStyleValueNow = cardStyleValues[i]
                updateHostView()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            // do nothing.
        }
    }

    private inner class SubtitleDataSpinnerSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            setBottomSheetState(subtitleDataValues[i] == "custom")
            if (subtitleDataValueNow != subtitleDataValues[i]) {
                subtitleDataValueNow = if (subtitleDataValues[i] == "custom") {
                    val editable = mSubtitleEditText!!.text
                    editable?.toString() ?: ""
                } else {
                    subtitleDataValues[i]
                }
                updateHostView()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            // do nothing.
        }
    }

    private inner class TextColorSpinnerSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            if (textColorValueNow != textColorValues[i]) {
                textColorValueNow = textColorValues[i]
                if (textColorValueNow != "auto") {
                    updateHostView()
                    return
                }
                var hasPermission = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hasPermission = checkPermissions(1)
                }
                if (hasPermission) {
                    updateHostView()
                }
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            // do nothing.
        }
    }

    private inner class ClockFontSpinnerSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            if (clockFontValueNow != clockFontValues[i]) {
                clockFontValueNow = clockFontValues[i]
                updateHostView()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            // do nothing.
        }
    }
}
