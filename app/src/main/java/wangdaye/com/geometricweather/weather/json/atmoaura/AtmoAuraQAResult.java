package wangdaye.com.geometricweather.weather.json.atmoaura;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Atmo Aura
 * */

public class AtmoAuraQAResult {
    @SerializedName("bon_geste")
    public Advice advice;
    @SerializedName("indices")
    public MultiDaysIndexs indexs;
    @SerializedName("dispositif")
    public Measure measure;

    public static class Advice {
        @SerializedName("contextes")
        public List<AdviceContext> contextList;
        @SerializedName("visuel")
        public String iconUrl;
        @SerializedName("message_long")
        public String messageLong;
        @SerializedName("message_court")
        public String messageShort;
        @SerializedName("saison")
        public String season;
        @SerializedName("type")
        public String type;

        public static class AdviceContext {
            @SerializedName("niveau")
            public String level;
            @SerializedName("population")
            public String[] population;
        }
    }

    public static class MultiDaysIndexs {
        @SerializedName("indice_j")
        public MultiIndex today;
        @SerializedName("indice_j+1")
        public MultiIndex tomorrow;
        @SerializedName("indice_j+2")
        public MultiIndex inTwoDays;
        @SerializedName("indice_j-1")
        public MultiIndex yesterday;

        public static class MultiIndex {
            @SerializedName("precision")
            public String accuracy;
            @SerializedName("indice_multipolluant")
            public Index aggregatedIndex;
            @SerializedName("date")
            public Date date;
            @SerializedName("sous_indice_no2")
            public Index no2;
            @SerializedName("sous_indice_o3")
            public Index o3;
            @SerializedName("sous_indice_pm10")
            public Index pm10;
            @SerializedName("type")
            public String type;

            public static class Index {
                @SerializedName("couleur_html")
                public String color;
                @SerializedName("qualificatif")
                public String quali;
                @SerializedName("valeur")
                public double val;
            }
        }
    }

    public static class Measure {
        @SerializedName("commentaire")
        public String comment;
        @SerializedName("date_fin")
        public Date endDate;
        @SerializedName("niveau")
        public String level;
        @SerializedName("date_modification")
        public Date modificationDate;
        @SerializedName("nom_procedure")
        public String name;
        @SerializedName("polluant")
        public String pollutant;
        @SerializedName("date_debut")
        public Date startDate;
        @SerializedName("seuil")
        public String threshold;
        @SerializedName("zone")
        public String zone;
    }
}