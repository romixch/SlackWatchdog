package ch.apptiva.watchdog;

import static ch.apptiva.watchdog.WatchStateEnum.NOK;
import static ch.apptiva.watchdog.WatchStateEnum.OK;
import static ch.apptiva.watchdog.WatchStateEnum.UNKNOWN;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class WatchedURI {

	private static final long POLLING_INTERVAL = 1000 * 60 * 30;
	private URI uri;
	private String channelNameToRespond;
	private WatchStateEnum currentState = UNKNOWN;
	private long lastCheckTimeMillis;

	public WatchedURI(URI uri, String channelNameToRespond) {
		this.uri = uri;
		this.channelNameToRespond = channelNameToRespond;
	}

	public URI getUri() {
		return uri;
	}

	public String getChannelNameToRespond() {
		return channelNameToRespond;
	}

	public WatchStateEnum getCurrentState() {
		return currentState;
	}

	public void performWatch(WatchEventListener listener) {
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastCheckTimeMillis < POLLING_INTERVAL) {
			return;
		}
		lastCheckTimeMillis = currentTimeMillis;
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(uri);
			RequestConfig config = RequestConfig.custom() //
					.setConnectionRequestTimeout(5000) //
					.setConnectTimeout(5000) //
					.setSocketTimeout(5000) //
					.build();
			httpGet.setConfig(config);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						reportState(listener, OK);
					} else {
						reportState(listener, NOK);
					}
					return null;
				}

			};
			httpclient.execute(httpGet, responseHandler);
		} catch (Exception e) {
			reportState(listener, NOK);
		}
	}

	private void reportState(WatchEventListener listener, WatchStateEnum newState) {
		if (!newState.equals(currentState)) {
			listener.stateChanged(currentState, newState, this);
			currentState = newState;
		}
	}

	public void resetTimer() {
		lastCheckTimeMillis = 0;
	}
}
