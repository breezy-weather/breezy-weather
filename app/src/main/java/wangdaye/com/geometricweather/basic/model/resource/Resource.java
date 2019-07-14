package wangdaye.com.geometricweather.basic.model.resource;

import androidx.annotation.NonNull;

public class Resource<T> implements Cloneable {

    @NonNull public final T data;
    @NonNull public final Status status;

    public enum Status {
        SUCCESS, ERROR, LOADING
    }

    protected Resource(@NonNull T data, @NonNull Status status) {
        this.data = data;
        this.status = status;
    }

    public Resource(@NonNull Resource<T> resource) {
        this.data = resource.data;
        this.status = resource.status;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(data, Status.SUCCESS);
    }

    public static <T> Resource<T> error(@NonNull T data) {
        return new Resource<>(data, Status.ERROR);
    }

    public static <T> Resource<T> loading(@NonNull T data) {
        return new Resource<>(data, Status.LOADING);
    }
}