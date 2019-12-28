package wangdaye.com.geometricweather.main.adapter.main;

import wangdaye.com.geometricweather.ui.adapter.TagAdapter;

public class MainTag implements TagAdapter.Tag {

    private String name;
    private Type type;

    public enum Type {TEMPERATURE, WIND, PRECIPITATION, AIR_QUALITY, UV_INDEX}

    public MainTag(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
