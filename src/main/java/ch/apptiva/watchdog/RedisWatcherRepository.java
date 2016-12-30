package ch.apptiva.watchdog;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import redis.clients.jedis.Jedis;

/**
 * Created by roman on 25.12.16.
 */
public class RedisWatcherRepository implements WatcherRepository {

    private final Jedis jedis;

    public RedisWatcherRepository(URI uri) {
        jedis = new Jedis(uri);
    }

    @Override
    public void persist(Collection<WatchedURI> watchedURIs) {
        watchedURIs.forEach(watchedUri -> {
            String persistentString = watchedUri.toPersistentString();
            String resp = jedis.set(watchedUri.getKey(), persistentString);
            System.out.println("Persisted URI with following response: " + resp);
        });
    }

    @Override
    public Collection<WatchedURI> load() {
        Set<String> keys = jedis.keys("*");
        List<WatchedURI> uris = keys.stream()
            .map(key -> jedis.get(key))
            .map(value -> WatchedURI.fromPersistentString(value))
            .collect(Collectors.toList());
        return uris;
    }
}
