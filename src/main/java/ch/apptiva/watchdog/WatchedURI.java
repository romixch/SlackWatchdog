package ch.apptiva.watchdog;

import static ch.apptiva.watchdog.WatchStateEnum.UNKNOWN;
import static ch.apptiva.watchdog.WatchStateEnum.HEALTHY;
import static ch.apptiva.watchdog.WatchStateEnum.UNWELL;
import static ch.apptiva.watchdog.WatchStateEnum.SICK;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchedURI implements Serializable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchedURI.class);
    private static final int UNWELL_TO_SICK_TREASHHOLD = 3;
    private URI uri;
    private String channelToRespond;
    private WatchStateEnum currentState = UNKNOWN;
    private String errorCause = "";
    private long lastCheckTimeMillis;
    private int unwellCount; // how many times was this url reported unwell
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public WatchedURI() {
        // for jackson json deserialization
    }

    public WatchedURI(URI uri, String channelToRespond) {
        this.uri = uri;
        this.channelToRespond = channelToRespond;
    }

    public URI getUri() {
        return uri;
    }

    public String getChannelToRespond() {
        return channelToRespond;
    }

    public void setChannelToRespond(String channelToRespond) {
        if (channelToRespond == null) {
            throw new IllegalStateException("You can't change the channel to respond if it is already set.");
        }
        this.channelToRespond = channelToRespond;
    }

    public WatchStateEnum getCurrentState() {
        return currentState;
    }

    void setCurrentState(WatchStateEnum currentState) {
        this.currentState = currentState;
    }

    @JsonIgnore
    public String getKey() {
        return getChannelToRespond() + ";" + getUri().toString();
    }

    public void performWatch(WatchEventListener listener) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastCheckTimeMillis < currentState.getPollingIntervalInMillis()) {
            return;
        }
        lastCheckTimeMillis = currentTimeMillis;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            RequestConfig config = RequestConfig.custom() //
                    .setConnectionRequestTimeout(10000) //
                    .setConnectTimeout(10000) //
                    .setSocketTimeout(10000) //
                    .build();
            httpGet.setConfig(config);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response)
                        throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        errorCause = "";
                        reportHealthy(listener);
                    } else {
                        errorCause = "Der Server antwortet mit dem Status-Code " + status + ".";
                        LOGGER.info("Error while checking URL " + WatchedURI.this.uri.toString()
                                + ". Response Status was " + status + ".");
                        reportUnwell(listener);
                    }
                    return null;
                }

            };
            httpclient.execute(httpGet, responseHandler);
        } catch (Exception e) {
            errorCause = "Der Server antwortet nicht. Folgender Fehler ist aufgetreten: " + e.getMessage()
                    + ". Mehr Informationen findest du im Bot Log";
            LOGGER.info("Error while checking URL " + this.uri.toString(), e);
            reportUnwell(listener);
        }
    }

    public String toPersistentString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportUnwell(WatchEventListener listener) {
        if (unwellCount < UNWELL_TO_SICK_TREASHHOLD) {
            unwellCount++;
            reportState(listener, UNWELL);
        } else {
            reportState(listener, SICK);
        }
    }

    private void reportHealthy(WatchEventListener listener) {
        unwellCount = 0;
        reportState(listener, HEALTHY);
    }

    private void reportState(WatchEventListener listener, WatchStateEnum newState) {
        if (!newState.equals(currentState)) {
            listener.stateChanged(currentState, newState, this);
            currentState = newState;
        }
    }

    public void resetStateAndRecheck() {
        lastCheckTimeMillis = 0;
        currentState = UNKNOWN;
    }

    public String getErrorCause() {
        return errorCause;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WatchedURI) {
            return uri.equals(WatchedURI.class.cast(obj).uri);
        }
        return false;
    }

    public static WatchedURI fromPersistentString(String string) {
        try {
            return MAPPER.readValue(string, WatchedURI.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
