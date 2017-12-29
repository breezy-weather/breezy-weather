package wangdaye.com.geometricweather.data.entity.model.about;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

/**
 * About app translator.
 * */

public class AboutAppTranslator {

    public String name;
    public String email;
    public int flagResId;

    private AboutAppTranslator(String name, String email, int flagResId) {
        this.name = name;
        this.email = email;
        this.flagResId = flagResId;
    }

    public static List<AboutAppTranslator> buildTranslatorList() {
        List<AboutAppTranslator> list = new ArrayList<>(7);
        list.add(new AboutAppTranslator("Mehmet Saygin Yilmaz", "memcos@gmail.com", R.drawable.flag_tr));
        list.add(new AboutAppTranslator("Ali D.", "siyaha@gmail.com", R.drawable.flag_tr));
        list.add(new AboutAppTranslator("benjamin Tourrel", "polo_naref@hotmail.fr", R.drawable.flag_fr));
        list.add(new AboutAppTranslator("Roman Adadurov", "orelars53@gmail.com", R.drawable.flag_ru));
        list.add(new AboutAppTranslator("Ken Berns", "ken.berns@yahoo.de", R.drawable.flag_de));
        list.add(new AboutAppTranslator("Milan Andrejić", "amikia@hotmail.com", R.drawable.flag_sr));
        list.add(new AboutAppTranslator("Miguel Torrijos", "migueltg352340@gmail.com", R.drawable.flag_es));
        list.add(new AboutAppTranslator("Andrea Carulli", "rctandrew100@gmail.com", R.drawable.flag_it));
        return list;
    }
}
