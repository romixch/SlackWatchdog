package ch.apptiva.watchdog;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class WatchedURITest {
    @Test
    public void testEquals() throws Exception {
        WatchedURI watchedUriA = new WatchedURI(new URI("http://www.apptiva.ch/"), "channel");
        WatchedURI watchedUriB = new WatchedURI(new URI("http://www.apptiva.ch/"), "channel");
        WatchedURI watchedUriDifferent = new WatchedURI(new URI("http://google.ch/"), "channel");

        assertTrue(watchedUriA.equals(watchedUriB));
        assertTrue(watchedUriB.equals(watchedUriA));
        assertFalse(watchedUriA.equals(watchedUriDifferent));
    }

}