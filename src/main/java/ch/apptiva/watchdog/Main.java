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
            Thread.sleep(60 * 1000);
            watcher.watch(new WatchEventListener() {
                @Override
                public void stateChanged(WatchStateEnum from, WatchStateEnum to, WatchedURI watchedURI) {
                    if (to.equals(UNWELL)) {
                        // We don't report unwell state
                        return;
                    }
                    String message;
                    if (from.equals(UNKNOWN) && to.equals(HEALTHY)) {
                        message = watchedURI.getUri().toString() + " ist erreichbar und antwortet.";
                    } else if (from.equals(SICK) && to.equals(HEALTHY)) {
                        message = "Hurra! " + watchedURI.getUri().toString() + " ist wieder erreichbar.";
                    } else if (SICK.equals(to)) {
                        message = "Uiiii! " + watchedURI.getUri().toString()
                                + " ist nicht erreichbar. Du schaust besser mal vorbei, bevor es Probleme gibt!\n"
                                + "Details: " + watchedURI.getErrorCause();
                    } else {
                        message = "Der Status von " + watchedURI.getUri().toString() + " änderte von " + from
                                + " nach " + to + ".\n" + "Details: " + watchedURI.getErrorCause();
                    }
                    SlackChannel channel = slackSession.findChannelByName(watchedURI.getChannelRespond());
                    if (channel == null) {
                        channel = slackSession.findChannelById(watchedURI.getChannelRespond());
                    }
                    slackSession.sendMessage(channel, message);
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
