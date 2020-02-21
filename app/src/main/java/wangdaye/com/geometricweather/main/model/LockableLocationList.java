package wangdaye.com.geometricweather.main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import wangdaye.com.geometricweather.basic.model.location.Location;

public class LockableLocationList {

    private List<Location> locationList;
    private int currentPositionIndex;
    private int currentIndex;
    private ReadWriteLock lock;

    private Getter getter;
    private Setter setter;

    public class Getter {

        public List<Location> getLocationList() {
            return Collections.unmodifiableList(locationList);
        }

        public int getCurrentPositionIndex() {
            return currentPositionIndex;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }
    }

    public class Setter {

        public void setLocationList(List<Location> locationList) {
            LockableLocationList.this.locationList = locationList;
        }

        public void setCurrentPositionIndex(int currentPositionIndex) {
            LockableLocationList.this.currentPositionIndex = currentPositionIndex;
        }

        public void setCurrentIndex(int currentIndex) {
            LockableLocationList.this.currentIndex = currentIndex;
        }
    }

    public LockableLocationList() {
        this.locationList = new ArrayList<>();
        this.currentPositionIndex = -1;
        this.currentIndex = -1;
        this.lock = new ReentrantReadWriteLock();

        this.getter = new Getter();
        this.setter = new Setter();
    }

    public void read(Reader r) {
        lock.readLock().lock();
        r.read(getter);
        lock.readLock().unlock();
    }

    public void write(Writer w) {
        lock.writeLock().lock();
        w.write(getter, setter);
        lock.writeLock().unlock();
    }

    public interface Reader {
        void read(Getter getter);
    }

    public interface Writer {
        void write(Getter getter, Setter setter);
    }
}
