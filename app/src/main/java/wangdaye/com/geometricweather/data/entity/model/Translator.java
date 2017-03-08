package wangdaye.com.geometricweather.data.entity.model;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

/**
 * Translator.
 * */

public class Translator {
    // data
    public String name;
    public String email;
    public int flagResId;

    /** <br> life cycle. */

    private Translator(String name, String email, int flagResId) {
        this.name = name;
        this.email = email;
        this.flagResId = flagResId;
    }

    public static List<Translator> buildTranslatorList() {
        List<Translator> translatorList = new ArrayList<>(1);

        translatorList.add(new Translator("Mehmet Saygin Yilmaz", "memcos@gmail.com", R.drawable.flag_tr));
        translatorList.add(new Translator("benjamin Tourrel", "polo_naref@hotmail.fr", R.drawable.flag_fr));
        translatorList.add(new Translator("Roman Adadurov", "orelars53@gmail.com", R.drawable.flag_ru));

        return translatorList;
    }
}
