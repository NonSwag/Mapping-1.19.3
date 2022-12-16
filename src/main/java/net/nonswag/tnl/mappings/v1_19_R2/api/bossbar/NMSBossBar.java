package net.nonswag.tnl.mappings.v1_19_R2.api.bossbar;

import lombok.Getter;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.tnl.listener.api.bossbar.TNLBossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_19_R2.boss.CraftBossBar;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@Getter
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NMSBossBar extends TNLBossBar {
    private final String id;
    private String text;
    private BarColor color;
    private BarStyle style;
    private BarFlag[] barFlags;
    private double progress;
    private final CraftBossBar bossBar;

    public NMSBossBar(String id, String text, BarColor color, BarStyle style, double progress, BarFlag... barFlags) {
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
    public NMSBossBar setText(String text) {
        getBossBar().setTitle(text);
        this.text = text;
        return this;
    }

    @Override
    public NMSBossBar setColor(BarColor color) {
        getBossBar().setColor(color);
        this.color = color;
        return this;
    }

    @Override
    public NMSBossBar setStyle(BarStyle style) {
        getBossBar().setStyle(style);
        this.style = style;
        return this;
    }

    @Override
    public NMSBossBar setBarFlags(BarFlag... barFlags) {
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

    @Override
    public NMSBossBar setProgress(double progress) {
        this.progress = progress;
        getBossBar().setProgress(getProgress());
        return this;
    }
}
