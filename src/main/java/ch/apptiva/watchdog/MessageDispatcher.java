package ch.apptiva.watchdog;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.LoggerFactory;

import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

public class MessageDispatcher {

  private static final String HALTE_EIN_AUGE_AUF = "halte ein auge auf";
  private static final String BITTE_PRUEFEN = "bitte prüfen";
  private static final String WAS_MACHST_DU = "was machst du";
  private static final String VERGISS = "vergiss";
  private boolean shutdownRequested;
  private boolean shutdownAsked;
  private boolean foundInstruction;

  private Watcher watcher;

  public MessageDispatcher(Watcher watcher) {
    this.watcher = watcher;
  }

  public boolean shutdownRequested() {
    return shutdownRequested;
  }

  public void dispatch(SlackMessagePosted event, SlackSession session) {
    if (someoneIsTalkingToMe(event, session)) {
      addRobotFace(event, session);
      foundInstruction = false;
      watchOutForNewWatches(event, session);
      watchOutForWatchListing(event, session);
      watchOutForForgettingListing(event, session);
      watchOutForTimerReset(event, session);
      watchOutForShutdown(event, session);
      if (!foundInstruction) {
        tellCapabilities(event, session);
      }
    }
  }

  private SlackMessageHandle<SlackMessageReply> addRobotFace(SlackMessagePosted event,
      SlackSession session) {
    return session.addReactionToMessage(event.getChannel(), event.getTimestamp(), "robot_face");
  }

  private boolean someoneIsTalkingToMe(SlackMessagePosted event, SlackSession session) {
    boolean isDirect = event.getChannel().isDirect();
    boolean meInMessage =
        event.getMessageContent().contains("<@" + session.sessionPersona().getId() + '>');
    return isDirect || meInMessage;
  }

  private void watchOutForNewWatches(SlackMessagePosted event, SlackSession session) {
    String message = sanitizeMessage(event, session);
    if (message.contains(HALTE_EIN_AUGE_AUF)) {
      foundInstruction = true;
      String url =
          message.substring(message.indexOf(HALTE_EIN_AUGE_AUF) + HALTE_EIN_AUGE_AUF.length(),
              message.length()).trim();
      url = url.replace("<", "").replace(">", "");
      try {
        WatchedURI watchedURI = new WatchedURI(new URI(url), event.getChannel().getName());
        watcher.addWatchedURI(watchedURI);
      } catch (URISyntaxException e) {
        LoggerFactory.getLogger(this.getClass()).warn("Bad URI", e);
        session.sendMessage(event.getChannel(), "Offenbar ist die URI nicht gültig: " + url);
      }
      session.sendMessage(event.getChannel(), "OK. Werde ich tun.");
    }
  }

  private void watchOutForWatchListing(SlackMessagePosted event, SlackSession session) {
    String message = sanitizeMessage(event, session);
    if (message.contains(WAS_MACHST_DU)) {
      foundInstruction = true;
      if (watcher.isIdle()) {
        session.sendMessage(event.getChannel(), "Gerade nichts...");
      } else {
        for (WatchedURI watchedURI : watcher.getAllWatchedURIs()) {
          session.sendMessage(event.getChannel(), "Ich halte ein Auge auf die folgenden URIs:");
          session.sendMessage(event.getChannel(), watchedURI.getUri().toString());
        }
      }
    }
  }

  private void watchOutForForgettingListing(SlackMessagePosted event, SlackSession session) {
    String message = sanitizeMessage(event, session);
    if (message.contains(VERGISS)) {
      foundInstruction = true;
      String url =
          message.substring(message.indexOf(VERGISS) + VERGISS.length(), message.length()).trim();
      url = url.replace("<", "").replace(">", "");
      WatchedURI watchedURI;
      try {
        watchedURI = new WatchedURI(new URI(url), event.getChannel().getName());
        if (watcher.removeWatchedURI(watchedURI)) {
          session.sendMessage(event.getChannel(), "OK. Ist vergessen.");
        } else {
          session.sendMessage(event.getChannel(), "Sorry, ich konnte " + url + " nicht finden.");
        }
      } catch (URISyntaxException e) {
        LoggerFactory.getLogger(this.getClass()).warn("Bad URI", e);
        session.sendMessage(event.getChannel(), "Offenbar ist die URI nicht gültig: " + url);
      }
    }
  }

  private void watchOutForTimerReset(SlackMessagePosted event, SlackSession session) {
    String message = sanitizeMessage(event, session);
    if (message.contains(BITTE_PRUEFEN)) {
      foundInstruction = true;
      session.sendMessage(event.getChannel(), "Wird gemacht...");
      watcher.resetTimers();
    }
  }

  private void watchOutForShutdown(SlackMessagePosted event, SlackSession session) {
    String message = sanitizeMessage(event, session).replace(":", "").trim();
    if (message.equals("shutdown")) {
      shutdownAsked = true;
      foundInstruction = true;
      session.sendMessage(event.getChannel(), "Wirklich?");
    } else if (shutdownAsked && message.equals("ja")) {
      foundInstruction = true;
      session.sendMessage(event.getChannel(), "Tschüss...");
      shutdownRequested = true;
    } else {
      shutdownAsked = false;
    }
  }

  private String sanitizeMessage(SlackMessagePosted event, SlackSession session) {
    String me = "<@" + session.sessionPersona().getId() + ">";
    return event.getMessageContent().replace(me, "").trim().toLowerCase();
  }

  private void tellCapabilities(SlackMessagePosted event, SlackSession session) {
    session.sendMessage(event.getChannel(), "Hi <@" + event.getSender().getId() + ">!\n" //
        + "Du, ich kann so einiges. Sende mir \"" + HALTE_EIN_AUGE_AUF
        + " http://www.apptiva.ch/index.html\" und ich informiere dich zuverlässig darüber, wenn die Site nicht mehr erreichbar ist oder das Problem wieder gelöst ist.\n"
        + "Mit einem \"" + BITTE_PRUEFEN + "\" checke ich alle Hosts noch einmal durch.\n" //
        + "Ich kann auch URLs wieder vergessen (vergiss <url>) oder dir sagen, welche URLs ich überwache (was machst du?).\n" //
        + "Du kannst mich auch mit \"shutdown\" stoppen.\n" //
        + "Vergiss nicht, mich immer direkt anzusprechen.");
  }
}
