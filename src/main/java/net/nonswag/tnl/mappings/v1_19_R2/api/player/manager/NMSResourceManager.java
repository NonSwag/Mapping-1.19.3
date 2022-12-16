package net.nonswag.tnl.mappings.v1_19_R2.api.player.manager;

import net.nonswag.tnl.listener.api.player.manager.ResourceManager;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNullableByDefault;

@ParametersAreNullableByDefault
public abstract class NMSResourceManager extends ResourceManager {

    public void setStatus(Action action) {
        this.action = action;
    }

    public void setResourcePackUrl(@Nonnull String url) {
        this.resourcePackUrl = url;
    }

    public void setResourcePackHash(String hash) {
        this.resourcePackHash = hash;
    }
}
