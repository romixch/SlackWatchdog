package ch.apptiva.watchdog;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

public class MessageDispatcher {

	private boolean shutdownRequested;
	private boolean shutdownAsked;

	public boolean shutdownRequested() {
		return shutdownRequested;
	}

	public void dispatch(SlackMessagePosted event, SlackSession session) {
		session.addReactionToMessage(event.getChannel(), event.getTimestamp(), "robot_face");

		if (event.getMessageContent().toLowerCase().equals("shutdown")) {
			shutdownAsked = true;
			session.sendMessage(event.getChannel(), "Wirklich?");
		} else if (shutdownAsked && event.getMessageContent().toLowerCase().equals("ja")) {
			shutdownRequested = true;
			session.sendMessage(event.getChannel(), "Tschüss...");
		} else {
			shutdownAsked = false;
		}
	}
}
