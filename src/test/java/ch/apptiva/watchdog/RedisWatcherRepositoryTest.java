package ch.apptiva.watchdog;

import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.*;

/**
 * Test the Redis implementation of the interface {@link WatcherRepository}.
 * Created by roman on 27.12.16.
 */
public class RedisWatcherRepositoryTest {

    RedisWatcherRepository repository;

    @Before
    public void setUp() throws URISyntaxException {
        String redisUrl = System.getenv("REDIS_URL");
        Assume.assumeThat(redisUrl, notNullValue());
        repository = new RedisWatcherRepository(new URI(redisUrl));
    }

    @Test
    public void testPersist() throws URISyntaxException {
        ArrayList<WatchedURI> uris = new ArrayList<>();
        uris.add(new WatchedURI(new URI("http://www.apptiva.ch/"), "channel1"));
        uris.add(new WatchedURI(new URI("http://www.botfabrik.ch/"), "channel2"));
        uris.add(new WatchedURI(new URI("http://www.webkiste.ch/"), "channel3"));
        uris.add(new WatchedURI(new URI("http://www.eris.ch/"), "channel4"));
        repository.persist(uris);

        Collection<WatchedURI> loadedUris = repository.load();

        org.junit.Assert.assertThat(loadedUris.size(), Matchers.greaterThanOrEqualTo(4));
        loadedUris.contains(uris.get(0));
        loadedUris.contains(uris.get(1));
        loadedUris.contains(uris.get(2));
        loadedUris.contains(uris.get(3));
    }
}
