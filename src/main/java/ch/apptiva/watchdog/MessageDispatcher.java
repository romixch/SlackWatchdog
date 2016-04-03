package ch.apptiva.watchdog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

public class MessageDispatcher {

	private static final String HALTE_EIN_AUGE_AUF = "halte ein auge auf ";
	private boolean shutdownRequested;
	private boolean shutdownAsked;

	private List<URL> urlsToWatch = new ArrayList<>();

	public MessageDispatcher() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
	}

	public boolean shutdownRequested() {
		return shutdownRequested;
	}

	public List<URL> getUrlsToWatch() {
		return urlsToWatch;
	}

	public void dispatch(SlackMessagePosted event, SlackSession session) {
		session.addReactionToMessage(event.getChannel(), event.getTimestamp(), "robot_face");

		watchOutForShutdown(event, session);

		watchOutForNewWatches(event, session);

	}

	private void watchOutForNewWatches(SlackMessagePosted event, SlackSession session) {
		String message = event.getMessageContent();
		if (message.toLowerCase().startsWith(HALTE_EIN_AUGE_AUF)) {
			String url = message.substring(HALTE_EIN_AUGE_AUF.length(), message.length());
			url = url.replace("<", "").replace(">", "");
			try {
				urlsToWatch.add(new URL(url));
			} catch (MalformedURLException e) {
				LoggerFactory.getLogger(this.getClass()).warn("Bad URL", e);
				session.sendMessage(event.getChannel(), "Offenbar ist die URL nicht gültig: " + url);
			}
			session.sendMessage(event.getChannel(), "OK. Werde ich tun.");
		}
	}

	private void watchOutForShutdown(SlackMessagePosted event, SlackSession session) {
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
