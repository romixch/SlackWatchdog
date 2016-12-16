package ch.apptiva.watchdog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Watcher {

    private WatcherRepository repo = new WatcherRepository();
    private Set<WatchedURI> watchedURIs = new HashSet<>();

    public void watch(WatchEventListener listener) {
        for (WatchedURI watchedURI : watchedURIs) {
            watchedURI.performWatch(new WatchEventListener() {
                @Override
                public void stateChanged(WatchStateEnum from, WatchStateEnum to, WatchedURI watchedURI) {
                    persist();
                    listener.stateChanged(from, to, watchedURI);
                }
            });
        }
    }

    public void addWatchedURI(WatchedURI watchedURI) {
        watchedURIs.add(watchedURI);
        persist();
    }

    public boolean removeWatchedURI(WatchedURI watchedURI) {
        Iterator<WatchedURI> it = watchedURIs.iterator();
        while (it.hasNext()) {
            WatchedURI uri = it.next();
            if (uri.getUri().equals(watchedURI.getUri())) {
                it.remove();
                persist();
                return true;
            }
        }
        return false;
    }

    public void resetTimers() {
        for (WatchedURI watchedURI : watchedURIs) {
            watchedURI.resetStateAndRecheck();
        }
        persist();
    }

    public boolean isIdle() {
        return watchedURIs.isEmpty();
    }

    public Collection<WatchedURI> getAllWatchedURIs() {
        return Collections.unmodifiableCollection(watchedURIs);
    }

    private void persist() {
        repo.persist(watchedURIs);
    }

    public void load() {
        watchedURIs = new HashSet<>(repo.load());
    }
}
