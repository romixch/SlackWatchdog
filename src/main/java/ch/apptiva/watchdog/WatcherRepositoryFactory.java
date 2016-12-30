package ch.apptiva.watchdog;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Create repository. If environment variable REDIS_URL is set, a {@link RedisWatcherRepository}
 * is returned.
 */
public class WatcherRepositoryFactory {

    public static WatcherRepository create() {
        String redisUrl = System.getenv("REDIS_URL");
        if (redisUrl == null) {
            System.out.println("Using file repository");
            return new FileWatcherRepository();
        } else {
            try {
                System.out.println("Using redis repository");
                return new RedisWatcherRepository(new URI(redisUrl));
            } catch (URISyntaxException e) {
                throw new RuntimeException((e));
            }
        }
    }
}
