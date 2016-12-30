package ch.apptiva.watchdog;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Base64;

import static java.util.Base64.*;
import static org.hamcrest.Matchers.*;
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

    @Test
    public void testCreateFromPersistentString() {
        WatchedURI watchedURI = WatchedURI.fromPersistentString("{\"uri\":\"https://www.apptiva.ch:666/path?param=42\",\"channelToRespond\":\"respondChannel\",\"currentState\":\"HEALTHY\",\"errorCause\":\"\"}");
        assertThat(watchedURI.getUri().getScheme(), is("https"));
        assertThat(watchedURI.getUri().getHost(), is("www.apptiva.ch"));
        assertThat(watchedURI.getUri().getPort(), is(666));
        assertThat(watchedURI.getUri().getSchemeSpecificPart(), is("//www.apptiva.ch:666/path?param=42"));
        assertThat(watchedURI.getCurrentState(), is(WatchStateEnum.HEALTHY));
    }

    @Test
    public void testConverterToPersistentString() throws URISyntaxException {
        WatchedURI watchedURI = new WatchedURI(new URI("https://apptiva.ch:443/path?param=42"), "respondChannel");
        watchedURI.setCurrentState(WatchStateEnum.UNWELL);
        String persistentString = watchedURI.toPersistentString();
        assertThat(persistentString, is("{\"uri\":\"https://apptiva.ch:443/path?param=42\",\"channelToRespond\":\"respondChannel\",\"currentState\":\"UNWELL\",\"errorCause\":\"\"}"));
    }

    @Test
    public void testPersistentStringBackAndForth() throws URISyntaxException {
        WatchedURI originalWatchedURI = new WatchedURI(new URI("https://www.botfabrik.ch/can-kundendienst-bot.html"), "flasdfioe45w");
        originalWatchedURI.setCurrentState(WatchStateEnum.UNWELL);
        String persistent = originalWatchedURI.toPersistentString();
        WatchedURI restoredWatchedURI = WatchedURI.fromPersistentString(persistent);
        assertThat(restoredWatchedURI, is(originalWatchedURI));
        assertThat(restoredWatchedURI.getCurrentState(), is(originalWatchedURI.getCurrentState()));
    }
}