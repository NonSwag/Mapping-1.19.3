package net.nonswag.tnl.mapping.v1_19_R2;

import net.nonswag.core.api.annotation.FieldsAreNullableByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.tnl.listener.api.bossbar.TNLBossBar;
import net.nonswag.tnl.listener.api.enchantment.Enchant;
import net.nonswag.tnl.listener.api.entity.EntityHelper;
import net.nonswag.tnl.listener.api.entity.TNLArmorStand;
import net.nonswag.tnl.listener.api.entity.TNLEntityPlayer;
import net.nonswag.tnl.listener.api.entity.TNLFallingBlock;
import net.nonswag.tnl.listener.api.item.ItemHelper;
import net.nonswag.tnl.listener.api.item.TNLItem;
import net.nonswag.tnl.listener.api.logger.LogManager;
import net.nonswag.tnl.listener.api.mapper.Mapping;
import net.nonswag.tnl.listener.api.packets.incoming.Incoming;
import net.nonswag.tnl.listener.api.packets.outgoing.Outgoing;
import net.nonswag.tnl.listener.api.player.GameProfile;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.api.plugin.PluginHelper;
import net.nonswag.tnl.listener.api.version.Version;
import net.nonswag.tnl.listener.api.world.WorldHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.bossbar.NMSBossBar;
import net.nonswag.tnl.mapping.v1_19_R2.api.enchantments.EnchantmentWrapper;
import net.nonswag.tnl.mapping.v1_19_R2.api.entity.NMSArmorStand;
import net.nonswag.tnl.mapping.v1_19_R2.api.entity.NMSEntityHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.entity.NMSEntityPlayer;
import net.nonswag.tnl.mapping.v1_19_R2.api.entity.NMSFallingBlock;
import net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSItemHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSPluginHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSWorldHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.item.NMSItem;
import net.nonswag.tnl.mapping.v1_19_R2.api.logger.NMSLogManager;
import net.nonswag.tnl.mapping.v1_19_R2.api.packets.incoming.IncomingPacketManager;
import net.nonswag.tnl.mapping.v1_19_R2.api.packets.outgoing.OutgoingPacketManager;
import net.nonswag.tnl.mapping.v1_19_R2.api.player.NMSPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.SpigotConfig;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@FieldsAreNullableByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mapping.Info(id = "Mapping-1.19.3", name = "Origin 1.19.3", authors = "NonSwag", description = "An official TNL-Production")
public class Mappings extends Mapping {
    private ItemHelper itemHelper;
    private PluginHelper pluginHelper;
    private PacketManager packetManager;
    private LogManager logManager;
    private EntityHelper entityHelper;

    public Mappings(File file) {
        super(file);
    }

    @Override
    public Version getVersion() {
        return Version.v1_19_3;
    }

    @Override
    public TNLPlayer createPlayer(Player player) {
        return new NMSPlayer(player);
    }

    @Override
    public TNLItem createItem(ItemStack itemStack) {
        return new NMSItem(itemStack);
    }

    @Override
    public TNLBossBar createBossBar(String id, String text, BarColor color, BarStyle style, double progress, BarFlag... flags) {
        return new NMSBossBar(id, text, color, style, progress, flags);
    }

    @Override
    public TNLFallingBlock createFallingBlock(Location location, Material material) {
        return new NMSFallingBlock(location, material);
    }

    @Override
    public TNLArmorStand createArmorStand(World world, double x, double y, double z, float yaw, float pitch) {
        return new NMSArmorStand(world, x, y, z, yaw, pitch);
    }

    @Override
    public TNLEntityPlayer createEntityPlayer(World world, double x, double y, double z, float yaw, float pitch, GameProfile gameProfile) {
        return new NMSEntityPlayer(world, x, y, z, yaw, pitch, gameProfile);
    }

    @Override
    public Enchant createEnchant(NamespacedKey key, String name, EnchantmentTarget target) {
        return new EnchantmentWrapper(key, name, target);
    }

    @Override
    public ItemHelper itemHelper() {
        return itemHelper == null ? itemHelper = new NMSItemHelper() : itemHelper;
    }

    @Override
    public PluginHelper pluginHelper() {
        return pluginHelper == null ? pluginHelper = new NMSPluginHelper() : pluginHelper;
    }

    @Override
    public WorldHelper worldHelper() {
        return new NMSWorldHelper();
    }

    @Override
    public LogManager logManager() {
        return logManager == null ? logManager = new NMSLogManager() : logManager;
    }

    @Override
    public EntityHelper entityHelper() {
        return entityHelper == null ? entityHelper = new NMSEntityHelper() : entityHelper;
    }

    @Override
    public PacketManager packetManager() {
        return packetManager == null ? packetManager = new PacketManager() {
            private Outgoing outgoing;
            private Incoming incoming;

            @Override
            public Outgoing outgoing() {
                return outgoing == null ? outgoing = new OutgoingPacketManager() : outgoing;
            }

            @Override
            public Incoming incoming() {
                return incoming == null ? incoming = new IncomingPacketManager() : incoming;
            }
        } : packetManager;
    }

    @Override
    public boolean bungeeCord() {
        return SpigotConfig.bungee;
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(String name, @Nullable String id) {
        return null;
    }
}
