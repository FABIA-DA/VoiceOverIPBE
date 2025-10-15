package at.htlleonding.fabia;

import at.htlleonding.fabia.callmanagement.*;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.PlaybackFinished;
import ch.loway.oss.ari4java.generated.models.RecordingFinished;
import ch.loway.oss.ari4java.generated.models.StasisStart;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;

public class AriEventHandler extends AriWSHelper {
    @Override
    public void onConnectionEvent(AriConnectionEvent event) {
        super.onConnectionEvent(event);
    }

    @Override
    protected void onStasisStart(StasisStart message) {
        super.onStasisStart(message);

        System.out.println("New call entered Stasis: " + message.getChannel().getName());
        CallSession session = new CallSession(
                message.getChannel().getId(),
                message.getChannel().getName(),
                CallState.Greeting
        );
        CallManager.getInstance().addSession(session);
        CallProcessor.process(session);
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        super.onRecordingFinished(message);

        System.out.println("Recording finished: " + message.getRecording().getName());
        String channelId = message.getRecording().getTarget_uri();
        CallSession session = CallManager.getInstance().getSession(channelId);

        CallAudio.playNext(session);
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        super.onPlaybackFinished(message);

        final String channelPrefix = "channel:";

        if(!message.getPlayback().getTarget_uri().startsWith(channelPrefix)){
            return;
        }

        System.out.println("Playback finished: " + message.getPlayback().getId());
        String channelId = message.getPlayback().getTarget_uri().substring(channelPrefix.length() + 1);
        CallSession session = CallManager.getInstance().getSession(channelId);

        CallAudio.playNext(session);
    }
}
