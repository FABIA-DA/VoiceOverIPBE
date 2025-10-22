package at.htlleonding.fabia.callmanagement;

import lombok.Getter;

public abstract class AudioItem {
    protected final String name;
    @Getter
    protected final String channelId;

    protected AudioItem(String name, String channelId) {
        this.name = name;
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }
    public abstract void start();
}
