package ch.apptiva.watchdog;

import com.fasterxml.jackson.databind.JsonNode;

import flowctrl.integration.slack.webapi.SlackWebApiClient;

public class MessageDispatcher {

	private SlackWebApiClient webApiClient;
	private boolean shutdownRequested;
	private boolean shutdownAsked;

	public MessageDispatcher(SlackWebApiClient webApiClient) {
		this.webApiClient = webApiClient;
	}

	public void dispatch(JsonNode jsonNode) {
		String channel = jsonNode.get("channel").asText();
		String user = jsonNode.get("user").asText();
		String ts = jsonNode.get("ts").asText();
		String text = jsonNode.get("text").asText();
		webApiClient.addReactionToMessage("thumbsup", channel, ts);
		webApiClient.postMessage(channel, "jaja...");

		if (text.toLowerCase().equals("shutdown")) {
			webApiClient.postMessage(channel, "Wirklich?");
			shutdownAsked = true;
		} else if (shutdownAsked && text.toLowerCase().equals("ja")) {
			shutdownRequested = true;
		} else {
			shutdownAsked = false;
		}
	}

	public boolean shutdownRequested() {
		return shutdownRequested;
	}
}
