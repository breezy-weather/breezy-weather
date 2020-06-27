package com.mbestavros.geometricweather.basic.model.option.unit;

public enum CloudCoverUnit {

    PERCENT("%");

    private String unitAbbreviation;

    CloudCoverUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getCloudCoverText(int percent) {
        return UnitUtils.formatInt(percent) + unitAbbreviation;
    }
}
