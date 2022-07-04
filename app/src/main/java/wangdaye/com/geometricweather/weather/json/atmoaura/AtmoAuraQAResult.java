package wangdaye.com.geometricweather.weather.json.atmoaura;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Atmo Aura
 * */

public class AtmoAuraQAResult {
    @SerializedName("datetime_echeance")
    public Date datetimeEcheance;
    @SerializedName("polluants")
    public List<Polluant> polluants;

    public static class Polluant {
        @SerializedName("polluant")
        public String polluant;
        @SerializedName("horaires")
        public List<Horaire> horaires;

        public static class Horaire {
            @SerializedName("datetime_echeance")
            public Date datetimeEcheance;
            @SerializedName("indice_atmo")
            public Integer indiceAtmo;
            @SerializedName("concentration")
            public Integer concentration;
            @SerializedName("couleur")
            public String couleur;
        }
    }
}