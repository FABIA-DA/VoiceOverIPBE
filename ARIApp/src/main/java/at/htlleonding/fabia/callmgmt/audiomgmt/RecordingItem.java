package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;

import java.util.UUID;

public final class RecordingItem extends AudioItem {

    public RecordingItem(String channelId) {
        super("rec-" + UUID.randomUUID(), channelId);
    }

    @Override
    public void start() {
        AriUtil.startRecording(channelId, getName());
        ActiveAudioRegistry.getInstance().registerRecording(this);
    }
}
