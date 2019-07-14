package wangdaye.com.geometricweather.basic.model.resource;

import androidx.annotation.NonNull;

import java.util.List;

public class ListResource<T> {

    @NonNull public final List<T> dataList;
    @NonNull public final Event event;

    private interface Event {}

    public ListResource(@NonNull List<T> dataList) {
        this(dataList, new DataSetChanged());
    }

    private ListResource(@NonNull List<T> dataList, @NonNull Event event) {
        this.dataList = dataList;
        this.event = event;
    }

    public static <T> ListResource<T> insertItem(@NonNull ListResource<T> current,
                                                 @NonNull T item, int index) {
        List<T> list = current.dataList;
        list.add(index, item);
        return new ListResource<>(list, new ItemInserted(index));
    }

    public static <T> ListResource<T> changeItem(@NonNull ListResource<T> current,
                                                 @NonNull T item, int index) {
        List<T> list = current.dataList;
        list.set(index, item);
        return new ListResource<>(list, new ItemChanged(index));
    }

    public static <T> ListResource<T> removeItem(@NonNull ListResource<T> current, int index) {
        List<T> list = current.dataList;
        list.remove(index);
        return new ListResource<>(list, new ItemRemoved(index));
    }

    // event.

    public static class DataSetChanged implements Event {
    }

    public static class ItemInserted implements Event {

        public int index;

        public ItemInserted(int index) {
            this.index = index;
        }
    }

    public static class ItemChanged implements Event {

        public int index;

        public ItemChanged(int index) {
            this.index = index;
        }
    }

    public static class ItemRemoved implements Event {

        public int index;

        public ItemRemoved(int index) {
            this.index = index;
        }
    }
}
