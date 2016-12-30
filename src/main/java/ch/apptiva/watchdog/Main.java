package ch.apptiva.watchdog;

import static ch.apptiva.watchdog.WatchStateEnum.HEALTHY;
import static ch.apptiva.watchdog.WatchStateEnum.SICK;
import static ch.apptiva.watchdog.WatchStateEnum.UNKNOWN;
import static ch.apptiva.watchdog.WatchStateEnum.UNWELL;

import java.io.IOException;
import java.net.URISyntaxException;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class Main {

    public static void main(String[] args)
            throws InterruptedException, IOException, URISyntaxException {
        String token = getToken(args);
        SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(token);
        slackSession.connect();
        Watcher watcher = new Watcher();
        watcher.load();
        MessageDispatcher dispatcher = new MessageDispatcher(watcher);

        slackSession.addMessagePostedListener(new SlackMessagePostedListener() {
            @Override
            public void onEvent(SlackMessagePosted event, SlackSession session) {
                if (!event.getSender().isBot()) {
                    dispatcher.dispatch(event, session);
                }
            }
        });

        while (true) {
            Thread.sleep(20 * 1000);
            watcher.watch(new WatchEventListener() {
                @Override
                public void stateChanged(WatchStateEnum from, WatchStateEnum to, WatchedURI watchedURI) {
                    String message = null;
                    if (from.equals(UNKNOWN) && to.equals(HEALTHY)) {
                        message = watchedURI.getUri().toString() + " ist erreichbar und antwortet.";
                    } else if (from.equals(UNKNOWN) && to.equals(UNWELL)) {
                        message = watchedURI.getUri().toString() + " ist gerade nicht erreichbar. Ich melde mich noch einmal wenn das weiter so bleibt.";
                    } else if (from.equals(SICK) && to.equals(HEALTHY)) {
                        message = "Hurra! " + watchedURI.getUri().toString() + " ist wieder erreichbar.";
                    } else if (SICK.equals(to)) {
                        message = "Uiiii! " + watchedURI.getUri().toString()
                                + " ist nicht erreichbar. Du schaust besser mal vorbei, bevor es Probleme gibt!\n"
                                + "Details: " + watchedURI.getErrorCause();
                    }
                    if (message != null) {
                        SlackChannel channel = slackSession.findChannelByName(watchedURI.getChannelToRespond());
                        if (channel == null) {
                            channel = slackSession.findChannelById(watchedURI.getChannelToRespond());
                        }
                        slackSession.sendMessage(channel, message);
                    }
                }
            });
            if (dispatcher.shutdownRequested()) {
                slackSession.disconnect();
                break;
            }
        }
    }

    private static String getToken(String[] args) {
        if (args.length > 0 && args[0] != null) {
            return args[0];
        } else if (System.getenv("slacktoken") !=null) {
            return System.getenv("slacktoken");
        } else {
            throw new IllegalStateException("I need a slack token. Either by program argument or by environment variable 'slacktoken'.");
        }
    }

}
