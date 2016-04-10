package ch.apptiva.watchdog;

import static ch.apptiva.watchdog.WatchStateEnum.NOK;
import static ch.apptiva.watchdog.WatchStateEnum.OK;
import static ch.apptiva.watchdog.WatchStateEnum.UNKNOWN;

import java.io.IOException;
import java.net.URISyntaxException;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class Main {

  public static void main(String[] args)
      throws InterruptedException, IOException, URISyntaxException {
    String token = args[0];
    SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(token);
    slackSession.connect();
    Watcher watcher = new Watcher();
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
          String message;
          if (OK.equals(to) && UNKNOWN.equals(from)) {
            message = watchedURI.getUri().toString() + " ist erreichbar und antwortet.";
          } else if (OK.equals(to) && NOK.equals(from)) {
            message = "Hurra! " + watchedURI.getUri().toString() + " ist wieder erreichbar.";
          } else if (NOK.equals(to)) {
            message = "Uiiii! " + watchedURI.getUri().toString()
                + " ist nicht erreichbar. Du schaust besser mal vorbei, bevor es Probleme gibt!\n"
                + "Details: " + watchedURI.getErrorCause();
          } else {
            message = "Der Status von " + watchedURI.getUri().toString() + " änderte von " + from
                + " nach " + to + ".\n" + "Details: " + watchedURI.getErrorCause();
          }
          slackSession.sendMessage(
              slackSession.findChannelByName(watchedURI.getChannelNameToRespond()), message);
        }
      });
      if (dispatcher.shutdownRequested()) {
        slackSession.disconnect();
        break;
      }
    }
  }

}
