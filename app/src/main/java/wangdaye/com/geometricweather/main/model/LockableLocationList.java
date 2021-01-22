package wangdaye.com.geometricweather.main.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import wangdaye.com.geometricweather.basic.model.Location;

public class LockableLocationList {

    private List<Location> totalList;
    private List<Location> validList;
    private int currentIndex;
    private String formattedId;
    private final ReadWriteLock lock;

    private final Getter getter;
    private final Setter setter;

    public class Getter {

        public List<Location> getTotalList() {
            return Collections.unmodifiableList(totalList);
        }

        public List<Location> getValidList() {
            return Collections.unmodifiableList(validList);
        }

        public int getValidCurrentIndex() {
            return currentIndex;
        }

        public String getValidFormattedId() {
            return formattedId;
        }
    }

    public class Setter {

        public void setLocationList(Context context, List<Location> totalList) {
            LockableLocationList.this.totalList = totalList;
            LockableLocationList.this.validList = Location.excludeInvalidResidentLocation(context, totalList);

            if (currentIndex < 0
                    || currentIndex >= validList.size()
                    || !validList.get(currentIndex).getFormattedId().equals(formattedId)) {
                for (int i = 0; i < validList.size(); i ++) {
                    if (validList.get(i).getFormattedId().equals(formattedId)) {
                        setCurrentIndex(i);
                        break;
                    }
                }
            }
        }

        public void setCurrentIndex(int currentIndex) {
            LockableLocationList.this.currentIndex = currentIndex;
            LockableLocationList.this.formattedId = validList.get(currentIndex).getFormattedId();
        }
    }

    public LockableLocationList() {
        this.totalList = new ArrayList<>();
        this.validList = new ArrayList<>();
        this.currentIndex = -1;
        this.formattedId = null;
        this.lock = new ReentrantReadWriteLock();

        this.getter = new Getter();
        this.setter = new Setter();
    }

    public void read(Reader r) {
        lock.readLock().lock();
        try {
            r.read(getter);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void write(Writer w) {
        lock.writeLock().lock();
        try {
            w.write(getter, setter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public interface Reader {
        void read(Getter getter);
    }

    public interface Writer {
        void write(Getter getter, Setter setter);
    }
}
