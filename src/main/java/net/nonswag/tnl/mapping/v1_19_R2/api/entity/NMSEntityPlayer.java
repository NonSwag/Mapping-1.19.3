package net.nonswag.tnl.mapping.v1_19_R2.api.entity;

import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.chat.ChatSession;
import net.nonswag.tnl.listener.api.entity.TNLEntityPlayer;
import net.nonswag.tnl.listener.api.gamemode.Gamemode;
import net.nonswag.tnl.listener.api.item.SlotType;
import net.nonswag.tnl.listener.api.item.TNLItem;
import net.nonswag.tnl.listener.api.player.GameProfile;
import net.nonswag.tnl.listener.api.player.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.jetbrains.annotations.Nullable;

import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.nullable;
import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.wrap;

public class NMSEntityPlayer implements TNLEntityPlayer {

    @Getter
    private final GameProfile gameProfile;
    private final ServerPlayer player;
    private boolean cape = false;

    public NMSEntityPlayer(World world, double x, double y, double z, float yaw, float pitch, GameProfile profile) {
        this.player = new ServerPlayer(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) world).getHandle(), wrap(profile));
        player.moveTo(x, y, z, yaw, pitch);
        Skin skin = profile.getSkin();
        if (skin != null) {
            player.gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }
        setCapeVisibility(false);
        this.gameProfile = profile;
    }

    @Override
    public void setItem(SlotType slot, TNLItem item) {
        player.setItemSlot(wrap(slot), CraftItemStack.asNMSCopy(item.getItemStack()), true);
    }

    @Override
    public void setPing(int ping) {
        player.latency = ping;
    }

    @Override
    public void setGlowing(boolean glowing) {
        player.setGlowingTag(glowing);
    }

    @Override
    public int getPing() {
        return player.latency;
    }

    @Override
    public void setPlayerPose(Pose pose) {
        player.setPose(switch (pose) {
            case SNEAKING -> net.minecraft.world.entity.Pose.CROUCHING;
            case DYING -> net.minecraft.world.entity.Pose.DYING;
            case FALL_FLYING -> net.minecraft.world.entity.Pose.FALL_FLYING;
            case SLEEPING -> net.minecraft.world.entity.Pose.SLEEPING;
            case SPIN_ATTACK -> net.minecraft.world.entity.Pose.SPIN_ATTACK;
            case DIGGING -> net.minecraft.world.entity.Pose.DIGGING;
            case ROARING -> net.minecraft.world.entity.Pose.ROARING;
            case CROAKING -> net.minecraft.world.entity.Pose.CROAKING;
            case EMERGING -> net.minecraft.world.entity.Pose.EMERGING;
            case SNIFFING -> net.minecraft.world.entity.Pose.SNIFFING;
            case SITTING -> net.minecraft.world.entity.Pose.SITTING;
            case LONG_JUMPING -> net.minecraft.world.entity.Pose.LONG_JUMPING;
            case USING_TONGUE -> net.minecraft.world.entity.Pose.USING_TONGUE;
            case STANDING -> net.minecraft.world.entity.Pose.STANDING;
            case SWIMMING -> net.minecraft.world.entity.Pose.SWIMMING;
        });
    }

    @Override
    public Gamemode getGamemode() {
        return wrap(player.gameMode.getGameModeForPlayer());
    }

    @Override
    public void setGamemode(Gamemode gamemode) {
        Reflection.Field.setByType(player.gameMode, GameType.class, player.gameMode.getGameModeForPlayer(), 1);
        Reflection.Field.setByType(player.gameMode, GameType.class, wrap(gamemode));
    }

    @Nullable
    @Override
    public ChatSession getChatSession() {
        return nullable(player.getChatSession());
    }

    @Override
    public Component getDisplayName() {
        return player.adventure$displayName;
    }

    @Override
    public Pose getPlayerPose() {
        return switch (player.getPose()) {
            case STANDING -> Pose.STANDING;
            case FALL_FLYING -> Pose.FALL_FLYING;
            case SLEEPING -> Pose.SLEEPING;
            case SWIMMING -> Pose.SWIMMING;
            case SPIN_ATTACK -> Pose.SPIN_ATTACK;
            case CROUCHING -> Pose.SNEAKING;
            case LONG_JUMPING -> Pose.LONG_JUMPING;
            case DYING -> Pose.DYING;
            case CROAKING -> Pose.CROAKING;
            case USING_TONGUE -> Pose.USING_TONGUE;
            case SITTING -> Pose.SITTING;
            case ROARING -> Pose.ROARING;
            case SNIFFING -> Pose.SNIFFING;
            case EMERGING -> Pose.EMERGING;
            case DIGGING -> Pose.DIGGING;
        };
    }

    @Override
    public void setCapeVisibility(boolean visible) {
        this.cape = visible;
        player.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (cape ? 127 : 126));
    }

    @Override
    public boolean getCapeVisibility() {
        return cape;
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public void setLocation(Location location) {
        player.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public void setLocation(double x, double y, double z) {
        player.moveTo(x, y, z);
    }

    @Override
    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        player.moveTo(x, y, z, yaw, pitch);
    }

    @Override
    public int getEntityId() {
        return player.getId();
    }

    @Override
    public CraftPlayer bukkit() {
        return player.getBukkitEntity();
    }
}
