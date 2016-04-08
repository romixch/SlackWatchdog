package ch.apptiva.watchdog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Watcher {

	private Set<WatchedURI> watchedURIs = new HashSet<>();

	public void watch(WatchEventListener listener) {
		for (WatchedURI watchedURI : watchedURIs) {
			watchedURI.performWatch(listener);
		}
	}

	public void addWatchedURI(WatchedURI watchedURI) {
		watchedURIs.add(watchedURI);
	}

	public void removeWatchedURI(WatchedURI watchedURI) {
		Iterator<WatchedURI> it = watchedURIs.iterator();
		while (it.hasNext()) {
			WatchedURI uri = it.next();
			if (uri.getUri().equals(watchedURI.getUri())) {
				it.remove();
				break;
			}
		}
	}

	public void resetTimers() {
		for (WatchedURI watchedURI : watchedURIs) {
			watchedURI.resetTimer();
		}
	}
}
