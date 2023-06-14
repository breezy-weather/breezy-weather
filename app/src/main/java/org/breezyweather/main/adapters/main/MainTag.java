package org.breezyweather.main.adapters.main;

import org.breezyweather.common.ui.adapters.TagAdapter;

public class MainTag implements TagAdapter.Tag {

    private final String name;
    private final Type type;

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
