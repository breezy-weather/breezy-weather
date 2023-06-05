package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Mf warning current phenomenons
 * */

public class MfWarningsResult {

    @SerializedName("update_time")
    public Date updateTime;
    @SerializedName("end_validity_time")
    public Date endValidityTime;
    @SerializedName("domain_id")
    public String domain;
    @SerializedName("color_max")
    public int maxColor;
    public List<WarningTimelaps> timelaps;
    @SerializedName("phenomenons_items")
    public List<PhenomenonMaxColor> phenomenonsItems;
    public List<WarningAdvice> advices;
    public List<WarningConsequence> consequences;
    @SerializedName("max_count_items")
    public List<WarningMaxCountItems> maxCountItems;
    public WarningComments comments;
    public WarningComments text;
    @SerializedName("text_avalanche")
    public WarningComments textAvalanche;

    public static class WarningAdvice {
        @SerializedName("phenomenon_max_color_id")
        public int phenomenoMaxColorId;
        @SerializedName("phenomenon_id")
        public int phenomenonId;
        @SerializedName("text_advice")
        public String textAdvice;
    }

    public static class WarningComments {
        @SerializedName("begin_time")
        public Date beginTime;
        @SerializedName("end_time")
        public Date endTime;
        @SerializedName("text_bloc_item")
        public List<WarningTextBlocItem> textBlocItems;

        public static class WarningTextBlocItem {
            public List<String> text;
            @SerializedName("text_html")
            public List<String> textHtml;
            public String title;
            @SerializedName("title_html")
            public String titleHtml;
        }
    }

    public static class WarningConsequence {
        @SerializedName("phenomenon_max_color_id")
        public int phenomenoMaxColorId;
        @SerializedName("phenomenon_id")
        public int phenomenonId;
        @SerializedName("text_consequence")
        public String textConsequence;
    }

    public static class WarningMaxCountItems {
        @SerializedName("color_id")
        public int colorId;
        public int count;
        @SerializedName("text_count")
        public String textCount;
    }

    public static class PhenomenonMaxColor {
        @SerializedName("phenomenon_max_color_id")
        public int phenomenoMaxColorId;
        @SerializedName("phenomenon_id")
        public int phenomenonId;
    }

    public static class WarningTimelaps {
        @SerializedName("phenomenon_id")
        public int phenomenonId;
        @SerializedName("timelaps_items")
        public List<WarningTimelapsItem> timelapsItems;

        public static class WarningTimelapsItem {
            @SerializedName("begin_time")
            public Date beginTime;
            @SerializedName("end_time")
            public Date endTime;
            @SerializedName("color_id")
            public int colorId;
        }
    }
}