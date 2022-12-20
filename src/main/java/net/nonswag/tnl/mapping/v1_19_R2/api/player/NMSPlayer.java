package net.nonswag.tnl.mapping.v1_19_R2.api.player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.nonswag.core.api.annotation.FieldsAreNullableByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.logger.Logger;
import net.nonswag.core.api.message.Message;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.Bootstrap;
import net.nonswag.tnl.listener.Listener;
import net.nonswag.tnl.listener.api.advancement.Toast;
import net.nonswag.tnl.listener.api.chat.ChatSession;
import net.nonswag.tnl.listener.api.entity.TNLEntity;
import net.nonswag.tnl.listener.api.entity.TNLEntityLiving;
import net.nonswag.tnl.listener.api.entity.TNLEntityPlayer;
import net.nonswag.tnl.listener.api.location.BlockLocation;
import net.nonswag.tnl.listener.api.mapper.Mapping;
import net.nonswag.tnl.listener.api.mods.labymod.LabyPlayer;
import net.nonswag.tnl.listener.api.packets.outgoing.*;
import net.nonswag.tnl.listener.api.player.Skin;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.api.player.manager.*;
import net.nonswag.tnl.listener.api.sign.SignMenu;
import net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper;
import net.nonswag.tnl.mapping.v1_19_R2.api.player.channel.PlayerChannelHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@FieldsAreNullableByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NMSPlayer extends TNLPlayer {
    private AdvancementManager advancementManager;
    private PermissionManager permissionManager;
    private DataManager dataManager;
    private LabyPlayer labymod;
    private SoundManager soundManager;
    private NPCFactory npcFactory;
    private HologramManager hologramManager;
    private Messenger messenger;
    private ScoreboardManager scoreboardManager;
    private InterfaceManager interfaceManager;
    private WorldManager worldManager;
    private EnvironmentManager environmentManager;
    private HealthManager healthManager;
    private CombatManager combatManager;
    private SkinManager skinManager;
    private InventoryManager inventoryManager;
    private DebugManager debugManager;
    private AttributeManager attributeManager;
    private MetaManager metaManager;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private ServerManager serverManager;
    private CinematicManger cinematicManger;
    private TitleManager titleManager;
    private ParticleManager particleManager;
    private BossBarManager bossBarManager;
    private CooldownManager cooldownManager;
    private ResourceManager resourceManager;
    private Pipeline pipeline;

    public NMSPlayer(Player player) {
        super(player);
    }

    private ServerPlayer nms() {
        return ((CraftPlayer) bukkit()).getHandle();
    }

    private ServerGamePacketListenerImpl playerConnection() {
        return nms().connection;
    }

    private ServerLevel worldServer() {
        return ((CraftWorld) bukkit().getWorld()).getHandle();
    }

    @Override
    public void setName(Plugin plugin, String name) {
        GameProfile profile = nms().gameProfile;
        Reflection.Field.set(profile, "name", name);
        Listener.getOnlinePlayers().forEach(all -> {
            all.abilityManager().hide(plugin, this);
            all.abilityManager().show(plugin, this);
        });
    }

    @Override
    public net.nonswag.tnl.listener.api.player.GameProfile getGameProfile() {
        return new net.nonswag.tnl.listener.api.player.GameProfile(getUniqueId(), getName(), skinManager().getSkin());
    }

    @Override
    public ChatSession getChatSession() {
        return NMSHelper.nullable(nms().getChatSession());
    }

    @Override
    public int getPing() {
        return nms().latency;
    }

    @Override
    public Pose getPlayerPose() {
        return switch (nms().getPose()) {
            case DIGGING -> Pose.DIGGING;
            case SITTING -> Pose.SITTING;
            case ROARING -> Pose.ROARING;
            case CROAKING -> Pose.CROAKING;
            case EMERGING -> Pose.EMERGING;
            case SNIFFING -> Pose.SNIFFING;
            case LONG_JUMPING -> Pose.LONG_JUMPING;
            case USING_TONGUE -> Pose.USING_TONGUE;
            case CROUCHING -> Pose.SNEAKING;
            case DYING -> Pose.DYING;
            case FALL_FLYING -> Pose.FALL_FLYING;
            case SLEEPING -> Pose.SLEEPING;
            case SPIN_ATTACK -> Pose.SPIN_ATTACK;
            case STANDING -> Pose.STANDING;
            case SWIMMING -> Pose.SWIMMING;
        };
    }

    @Override
    public void setPlayerPose(Pose pose) {
        nms().setPose(switch (pose) {
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
    public void setPing(int ping) {
        nms().latency = ping;
    }

    @Override
    public AdvancementManager advancementManager() {
        if(advancementManager == null) advancementManager = new AdvancementManager() {
            @Override
            public void sendToast(Toast toast) {
                Advancement advancement = Advancement.Builder.advancement().
                        display(new DisplayInfo(CraftItemStack.asNMSCopy(toast.getIcon()),
                                Component.literal(toast.getTitle()), Component.empty(), null,
                                switch (toast.getType()) {
                                    case GOAL -> FrameType.GOAL;
                                    case TASK -> FrameType.TASK;
                                    case CHALLENGE -> FrameType.CHALLENGE;
                                }, true, false, false)).rewards(AdvancementRewards.EMPTY).
                        addCriterion("trigger", new ImpossibleTrigger.TriggerInstance()).
                        build(new ResourceLocation("listener", "toast"));
                award(advancement);
                Mapping.get().sync(() -> revoke(advancement), 2);
            }

            private void award(Advancement advancement) {
                for (String[] criteria : advancement.getRequirements()) {
                    for (String criterion : criteria) nms().getAdvancements().award(advancement, criterion);
                }
            }

            private void revoke(Advancement advancement) {
                for (String[] criteria : advancement.getRequirements()) {
                    for (String criterion : criteria) nms().getAdvancements().revoke(advancement, criterion);
                }
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return advancementManager;
    }

    @Override
    public PermissionManager permissionManager() {
        if (permissionManager == null) permissionManager = new PermissionManager() {

            @Override
            public Map<String, Boolean> getPermissions() {
                Map<String, Boolean> permissions = Reflection.Field.get(attachment, "permissions");
                if (permissions == null) Reflection.Field.set(attachment, "permissions", permissions = new HashMap<>());
                return permissions;
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return permissionManager;
    }

    @Override
    public DataManager data() {
        if (dataManager == null) dataManager = new DataManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return dataManager;
    }

    @Override
    public LabyPlayer labymod() {
        if (labymod == null) labymod = new LabyPlayer() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return labymod;
    }

    @Override
    public SoundManager soundManager() {
        if (soundManager == null) soundManager = new SoundManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return soundManager;
    }

    @Override
    public NPCFactory npcFactory() {
        if (npcFactory == null) npcFactory = new NPCFactory() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return npcFactory;
    }

    @Override
    public HologramManager hologramManager() {
        if (hologramManager == null) hologramManager = new HologramManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return hologramManager;
    }

    @Override
    public Messenger messenger() {
        if (messenger == null) messenger = new Messenger() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return messenger;
    }

    @Override
    public ScoreboardManager scoreboardManager() {
        if (scoreboardManager == null) scoreboardManager = new ScoreboardManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return scoreboardManager;
    }

    @Override
    public InterfaceManager interfaceManager() {
        if (interfaceManager == null) interfaceManager = new InterfaceManager() {
            @Override
            public void openVirtualSignEditor(SignMenu signMenu) {
                closeGUI(false);
                Location loc = worldManager().getLocation();
                BlockLocation location = new BlockLocation(worldManager().getWorld(), loc.getBlockX(), Math.max(loc.getBlockY() - 5, 0), loc.getBlockZ());
                signMenu.setLocation(location);
                BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
                OpenSignEditorPacket editor = OpenSignEditorPacket.create(location);
                Material material = Material.getMaterial(signMenu.getType().name());
                CraftBlockData blockData = (CraftBlockData) Objects.requireNonNullElse(material, Material.SPRUCE_WALL_SIGN).createBlockData();
                SignBlockEntity tileEntitySign = new SignBlockEntity(position, blockData.getState());
                for (int line = 0; line < signMenu.getLines().length; line++) {
                    tileEntitySign.setMessage(line, Component.literal(Message.format(signMenu.getLines()[line], getPlayer())));
                }
                worldManager().sendBlockChange(location, blockData);
                PacketBuilder.of(Objects.requireNonNull(tileEntitySign.getUpdatePacket())).send(getPlayer());
                editor.send(getPlayer());
                this.signMenu = signMenu;
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return interfaceManager;
    }

    @Override
    public WorldManager worldManager() {
        if (worldManager == null) worldManager = new WorldManager() {
            @Override
            public boolean isInRain() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void strikeLightning(Location location, boolean effect, boolean sound) {
                LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, worldServer());
                lightning.setPos(location.getX(), location.getY(), location.getZ());
                lightning.setVisualOnly(effect);
                lightning.setSilent(!sound);
                PacketBuilder.of(lightning.getAddEntityPacket()).send(getPlayer());
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return worldManager;
    }

    @Override
    public EnvironmentManager environmentManager() {
        if (environmentManager == null) environmentManager = new EnvironmentManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return environmentManager;
    }

    @Override
    public HealthManager healthManager() {
        if (healthManager == null) healthManager = new HealthManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return healthManager;
    }

    @Override
    public CombatManager combatManager() {
        if (combatManager == null) combatManager = new CombatManager() {
            @Override
            public void exitCombat() {
                nms().onLeaveCombat();
            }

            @Override
            public void enterCombat() {
                nms().onEnterCombat();
            }

            @Override
            public void setKiller(@Nullable TNLPlayer player) {
                bukkit().setKiller(player != null ? player.bukkit() : null);
            }

            @Override
            public void setLastDamager(@Nullable LivingEntity damager) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public LivingEntity getLastDamager() {
                EntityDamageEvent cause = bukkit().getLastDamageCause();
                return cause != null ? (LivingEntity) cause.getEntity() : null;
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return combatManager;
    }

    @Override
    public SkinManager skinManager() {
        if (skinManager == null) skinManager = new SkinManager() {

            @Nullable
            private Skin skin = null;

            @Override
            public Skin getSkin() {
                if (this.skin == null) {
                    GameProfile profile = nms().gameProfile;
                    Collection<Property> textures = profile.getProperties().get("textures");
                    for (Property texture : textures) {
                        this.skin = new Skin(texture.getValue(), texture.getSignature());
                        break;
                    }
                    this.skin = Skin.getSkin(getPlayer().getUniqueId());
                }
                return skin;
            }

            @Override
            public void disguise(TNLEntity entity, TNLPlayer receiver) {
                if (getPlayer().equals(receiver)) return;
                RemoveEntitiesPacket.create(getPlayer().bukkit()).send(receiver);
                int id = entity.getEntityId();
                RemoveEntitiesPacket.create(id).send(receiver);
                if (entity instanceof TNLEntityPlayer player) {
                    PlayerInfoRemovePacket.create(player.getGameProfile().getUniqueId()).send(receiver);
                    Reflection.Field.set(entity, Entity.class, "id", getPlayer().getEntityId());
                    PlayerInfoUpdatePacket.Entry entry = new PlayerInfoUpdatePacket.Entry(player);
                    PlayerInfoUpdatePacket.create(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry).send(receiver);
                    AddPlayerPacket.create(player).send(receiver);
                } else if (entity instanceof TNLEntityLiving livingEntity) {
                    Reflection.Field.set(entity, Entity.class, "id", getPlayer().getEntityId());
                    LivingEntitySpawnPacket.create(livingEntity.bukkit()).send(receiver);
                    SetEquipmentPacket.create(livingEntity.bukkit()).send(receiver);
                } else {
                    Reflection.Field.set(entity, Entity.class, "id", getPlayer().getEntityId());
                    AddEntityPacket.create(entity.bukkit()).send(receiver);
                }
                EntityMetadataPacket.create(entity.bukkit()).send(receiver);
                EntityHeadRotationPacket.create(entity.bukkit()).send(receiver);
                Reflection.Field.set(entity, Entity.class, "id", id);
            }

            @Override
            public void setCapeVisibility(boolean visible) {
                cape = visible;
                nms().getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (cape ? 127 : 126));
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return skinManager;
    }

    @Override
    public InventoryManager inventoryManager() {
        if (inventoryManager == null) inventoryManager = new InventoryManager() {
            @Override
            public void dropItem(ItemStack item, Consumer<org.bukkit.entity.Item> after) {
                Bootstrap.getInstance().sync(() -> {
                    ItemEntity drop = nms().drop(CraftItemStack.asNMSCopy(item), true, true, false);
                    if (!(drop instanceof org.bukkit.entity.Item)) return;
                    after.accept((org.bukkit.entity.Item) drop.getBukkitEntity());
                });
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return inventoryManager;
    }

    @Override
    public DebugManager debugManager() {
        if (debugManager == null) debugManager = new DebugManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return debugManager;
    }

    @Override
    public AttributeManager attributeManager() {
        if (attributeManager == null) attributeManager = new AttributeManager() {
            @Override
            public AttributeInstance getAttribute(Attribute attribute) {
                AttributeInstance instance = bukkit().getAttribute(attribute);
                assert instance != null;
                return instance;
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return attributeManager;
    }

    @Override
    public MetaManager metaManager() {
        if (metaManager == null) metaManager = new MetaManager() {
            @Override
            public void setFlag(int flag, boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean getFlag(int flag) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return metaManager;
    }

    @Override
    public EffectManager effectManager() {
        if (effectManager == null) effectManager = new EffectManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return effectManager;
    }

    @Override
    public AbilityManager abilityManager() {
        if (abilityManager == null) abilityManager = new AbilityManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return abilityManager;
    }

    @Override
    public ServerManager serverManager() {
        if (serverManager == null) serverManager = new ServerManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return serverManager;
    }

    @Override
    public CinematicManger cinematicManger() {
        if (cinematicManger == null) cinematicManger = new CinematicManger() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return cinematicManger;
    }

    @Override
    public TitleManager titleManager() {
        if (titleManager == null) titleManager = new TitleManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return titleManager;
    }

    @Override
    public ParticleManager particleManager() {
        if (particleManager == null) particleManager = new ParticleManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return particleManager;
    }

    @Override
    public BossBarManager bossBarManager() {
        if (bossBarManager == null) bossBarManager = new BossBarManager() {
            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return bossBarManager;
    }

    @Override
    public CooldownManager cooldownManager() {
        if (cooldownManager == null) cooldownManager = new CooldownManager() {
            @Override
            public void resetAttackCooldown() {
                setAttackCooldown(0);
            }

            @Override
            public void setAttackCooldown(float cooldown) {
                nms().oAttackAnim = cooldown;
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return cooldownManager;
    }

    @Override
    public ResourceManager resourceManager() {
        if (resourceManager == null) resourceManager = new ResourceManager() {
            @Override
            public NMSPlayer getPlayer() {
                return NMSPlayer.this;
            }
        };
        return resourceManager;
    }

    @Override
    public Pipeline pipeline() {
        return pipeline == null ? pipeline = new Pipeline() {

            private final String name = getName() + "-TNLListener";
            @Getter
            private boolean injected = false;

            @Override
            public <P> void sendPacket(P p, @Nullable net.nonswag.tnl.listener.api.packets.PacketSendListener listener) {
                if (p instanceof Packet<?> packet) playerConnection().send(packet, listener != null ? new PacketSendListener() {
                    @Override
                    public void onSuccess() {
                        listener.onSuccess(getPlayer());
                    }

                    @Nullable
                    @Override
                    public Packet<?> onFailure() {
                        return listener.onFailure(getPlayer());
                    }
                } : null);
                else throw new IllegalArgumentException("<'%s'> is not a packet".formatted(p.getClass().getName()));
            }

            @Override
            public void uninject() {
                try {
                    Channel channel = nms().connection.connection.channel;
                    if (channel.pipeline().get(name) != null) {
                        channel.eventLoop().submit(() -> channel.pipeline().remove(name));
                    }
                    data().export();
                } catch (Exception e) {
                    Logger.error.println(e);
                } finally {
                    players.remove(bukkit());
                    injected = false;
                }
            }

            @Override
            public void inject() {
                try {
                    ChannelPipeline pipeline = nms().connection.connection.channel.pipeline();
                    pipeline.addBefore("packet_handler", name, new PlayerChannelHandler() {
                        @Override
                        public TNLPlayer getPlayer() {
                            return NMSPlayer.this;
                        }
                    });
                    injected = true;
                } catch (Exception e) {
                    Logger.error.println(e);
                    uninject();
                }
            }

            @Override
            public TNLPlayer getPlayer() {
                return NMSPlayer.this;
            }
        } : pipeline;
    }
}
