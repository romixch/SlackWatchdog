package ch.apptiva.watchdog;


import java.util.Collection;
import javax.validation.constraints.NotNull;

/**
 * Created by roman on 25.12.16.
 */
public interface WatcherRepository {
    void persist(Collection<WatchedURI> watchedURIs);

    @NotNull
    Collection<WatchedURI> load();
}
