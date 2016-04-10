package ch.apptiva.watchdog;

public interface WatchEventListener {

  void stateChanged(WatchStateEnum from, WatchStateEnum to, WatchedURI watchedURI);
}
