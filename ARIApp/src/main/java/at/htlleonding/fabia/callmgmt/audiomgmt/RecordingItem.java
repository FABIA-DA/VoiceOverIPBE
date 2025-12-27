package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;

import java.util.UUID;

/**
 * Represents data for a recording, which is created by asterisk
 */
public final class RecordingItem extends AudioItem {

    public RecordingItem(String channelId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        super("rec-" + UUID.randomUUID(), channelId, ariUtil, audioRegistry);
    }

    @Override
    public void start() {
        ariUtil.startRecording(channelId, getName());
        audioRegistry.registerRecording(this);
    }
}
