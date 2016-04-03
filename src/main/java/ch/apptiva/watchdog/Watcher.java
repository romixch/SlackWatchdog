package ch.apptiva.watchdog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.ullink.slack.simpleslackapi.SlackSession;

public class Watcher {

	private SlackSession slackSession;

	public Watcher(SlackSession slackSession) {
		this.slackSession = slackSession;
	}

	public void watch(URL url) throws URISyntaxException, IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(url.toURI());
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						slackSession.sendMessageToUser("romix", url + " ist prima erreichbar.", null);
					} else {
						slackSession.sendMessageToUser("romix", "mit " + url + " stimmt etwas nicht.", null);
					}
					return null;
				}
			};
			httpclient.execute(httpGet, responseHandler);
		} catch (Exception e) {
			slackSession.sendMessageToUser("romix", "mit " + url + " stimmt etwas nicht", null);
		}

	}
}
