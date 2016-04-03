package ch.apptiva.watchdog;

import java.io.IOException;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class Main {

	public static void main(String[] args) throws InterruptedException, IOException {
		String token = args[0];
		SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(token);
		slackSession.connect();
		MessageDispatcher dispatcher = new MessageDispatcher();

		slackSession.addMessagePostedListener(new SlackMessagePostedListener() {
			@Override
			public void onEvent(SlackMessagePosted event, SlackSession session) {
				if (!event.getSender().isBot()) {
					dispatcher.dispatch(event, session);
				}
			}
		});
		while (true) {
			Thread.sleep(5000);
			if (dispatcher.shutdownRequested()) {
				slackSession.disconnect();
				break;
			}
		}
	}

}
