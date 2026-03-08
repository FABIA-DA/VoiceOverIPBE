package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

/**
 * Represents data for a recording, which is created by asterisk
 */
public final class RecordingItem extends AudioItem {

    public RecordingItem(String bridgeId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        super("rec-" + UUID.randomUUID(), bridgeId, ariUtil, audioRegistry);
    }

    @Override
    public Uni<Void> startAsync() {
        audioRegistry.registerRecording(this);
        return ariUtil.startRecordingAsync(channelId, getName());
    }
}
