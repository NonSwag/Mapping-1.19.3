package net.nonswag.tnl.mappings.v1_19_R3.api.player.manager;

import net.nonswag.tnl.listener.api.player.manager.ResourceManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NMSResourceManager extends ResourceManager {

    public void setStatus(@Nullable Action action) {
        this.action = action;
    }

    public void setResourcePackUrl(@Nonnull String url) {
        this.resourcePackUrl = url;
    }

    public void setResourcePackHash(@Nullable String hash) {
        this.resourcePackHash = hash;
    }
}
