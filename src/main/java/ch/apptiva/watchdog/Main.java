package ch.apptiva.watchdog;

import com.fasterxml.jackson.databind.JsonNode;

import flowctrl.integration.slack.SlackClientFactory;
import flowctrl.integration.slack.rtm.Event;
import flowctrl.integration.slack.rtm.EventListener;
import flowctrl.integration.slack.rtm.SlackRealTimeMessagingClient;
import flowctrl.integration.slack.webapi.SlackWebApiClient;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		String token = args[0];
		SlackWebApiClient webApiClient = SlackClientFactory.createWebApiClient(token);
		String webSocketUrl = webApiClient.startRealTimeMessagingApi();
		MessageDispatcher dispatcher = new MessageDispatcher(webApiClient);

		SlackRealTimeMessagingClient rtmClient = new SlackRealTimeMessagingClient(webSocketUrl, null, null);
		rtmClient.connect();
		rtmClient.addListener(Event.MESSAGE, new EventListener() {
			@Override
			public void handleMessage(JsonNode jsonNode) {
				dispatcher.dispatch(jsonNode);
			}
		});
		while (true) {
			Thread.sleep(5000);
			if (dispatcher.shutdownRequested()) {
				rtmClient.close();
				webApiClient.shutdown();
				break;
			}
		}
	}

}
