package ch.apptiva.watchdog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.LoggerFactory;

public class WatcherRepository {

  public void persist(Collection<WatchedURI> watchedURIs) {
    try {
      File store = getStore();
      if (store.exists()) {
        store.delete();
      }
      try (FileOutputStream fos = new FileOutputStream(store)) {
        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
          oos.writeInt(watchedURIs.size());
          for (WatchedURI watchedURI : watchedURIs) {
            oos.writeObject(watchedURI);
          }
        }
      }
    } catch (URISyntaxException | IOException e) {
      LoggerFactory.getLogger(getClass()).error("Can't persist watched URIs.", e);
    }
  }

  private File getStore() throws URISyntaxException {
    File path =
        new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
            .getParentFile();
    File store = new File(path, "watchedURIs.bin");
    return store;
  }

  public Collection<WatchedURI> load() {
    HashSet<WatchedURI> uris = new HashSet<WatchedURI>();
    try {
      File store = getStore();
      if (store.exists()) {
        try (FileInputStream fis = new FileInputStream(store)) {
          try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            int size = ois.readInt();
            Object o;
            for (int s = 0; s < size; s++) {
              o = ois.readObject();
              if (o instanceof WatchedURI) {
                uris.add((WatchedURI) o);
              }
            }
          }
        }
      }
    } catch (URISyntaxException | IOException | ClassNotFoundException e) {
      LoggerFactory.getLogger(getClass()).error("Can't persist watched URIs.", e);
    }
    return uris;
  }
}
