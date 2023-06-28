package org.breezyweather.location;

public class LocationException extends Exception {

    public LocationException(int code, String msg) {
        super(msg + "(code = " + code + ")");
    }
}
