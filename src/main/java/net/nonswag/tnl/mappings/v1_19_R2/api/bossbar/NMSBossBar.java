package net.nonswag.tnl.mappings.v1_19_R3.api.bossbar;

import net.nonswag.tnl.listener.api.bossbar.TNLBossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_19_R2.boss.CraftBossBar;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class NMSBossBar extends TNLBossBar {

    @Nonnull
    private final String id;
    @Nonnull
    private String text;
    @Nonnull
    private BarColor color;
    @Nonnull
    private BarStyle style;
    @Nonnull
    private BarFlag[] barFlags;
    private double progress;
    @Nonnull
    private final CraftBossBar bossBar;

    public NMSBossBar(@Nonnull String id, @Nonnull String text, @Nonnull BarColor color, @Nonnull BarStyle style, double progress, @Nonnull BarFlag... barFlags) {
        this.id = id;
        this.text = text;
        this.color = color;
        this.style = style;
        this.barFlags = barFlags;
        this.progress = progress <= 0 ? 0 : progress >= 1 ? 1 : progress;
        this.bossBar = new CraftBossBar(getText(), getColor(), getStyle(), getBarFlags());
        setProgress(getProgress());
    }

    @Override
    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public String getText() {
        return text;
    }

    @Nonnull
    @Override
    public BarColor getColor() {
        return color;
    }

    @Nonnull
    @Override
    public BarStyle getStyle() {
        return style;
    }

    @Nonnull
    @Override
    public BarFlag[] getBarFlags() {
        return barFlags;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Nonnull
    @Override
    public NMSBossBar setText(@Nonnull String text) {
        getBossBar().setTitle(text);
        this.text = text;
        return this;
    }

    @Nonnull
    @Override
    public NMSBossBar setColor(@Nonnull BarColor color) {
        getBossBar().setColor(color);
        this.color = color;
        return this;
    }

    @Nonnull
    @Override
    public NMSBossBar setStyle(@Nonnull BarStyle style) {
        getBossBar().setStyle(style);
        this.style = style;
        return this;
    }

    @Nonnull
    @Override
    public NMSBossBar setBarFlags(@Nonnull BarFlag... barFlags) {
        for (BarFlag flag : BarFlag.values()) {
            if (Arrays.asList(barFlags).contains(flag)) {
                getBossBar().addFlag(flag);
            } else {
                getBossBar().removeFlag(flag);
            }
        }
        this.barFlags = barFlags;
        return this;
    }

    @Nonnull
    @Override
    public NMSBossBar setProgress(double progress) {
        this.progress = progress;
        getBossBar().setProgress(getProgress());
        return this;
    }

    @Nonnull
    @Override
    public CraftBossBar getBossBar() {
        return bossBar;
    }
}
