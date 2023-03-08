package net.nonswag.tnl.mapping.v1_19_R2.api.packets.outgoing;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.advancement.Advancement;
import net.nonswag.tnl.listener.api.border.VirtualBorder;
import net.nonswag.tnl.listener.api.chat.ChatType;
import net.nonswag.tnl.listener.api.chat.Filter;
import net.nonswag.tnl.listener.api.chat.SignedMessageBody;
import net.nonswag.tnl.listener.api.item.SlotType;
import net.nonswag.tnl.listener.api.item.TNLItem;
import net.nonswag.tnl.listener.api.location.BlockPosition;
import net.nonswag.tnl.listener.api.location.Position;
import net.nonswag.tnl.listener.api.nbt.NBTTag;
import net.nonswag.tnl.listener.api.packets.outgoing.*;
import net.nonswag.tnl.listener.api.player.Hand;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_19_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;

import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.nullable;
import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.wrap;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OutgoingPacketManager implements Outgoing {
    private static final Logger logger = LoggerFactory.getLogger(OutgoingPacketManager.class);

    @Override
    public SetSimulationDistancePacket setSimulationDistancePacket(int simulationDistance) {
        return new SetSimulationDistancePacket(simulationDistance) {
            @Override
            public ClientboundSetSimulationDistancePacket build() {
                return new ClientboundSetSimulationDistancePacket(getSimulationDistance());
            }
        };
    }

    @Override
    public SetCarriedItemPacket setCarriedItemPacket(int slot) {
        return new SetCarriedItemPacket(slot) {
            @Override
            public ClientboundSetCarriedItemPacket build() {
                return new ClientboundSetCarriedItemPacket(getSlot());
            }
        };
    }

    @Override
    public SetDisplayObjectivePacket setDisplayObjectivePacket(int slot, @Nullable String objectiveName) {
        return new SetDisplayObjectivePacket(slot, objectiveName) {
            @Override
            public ClientboundSetDisplayObjectivePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeByte(getSlot());
                buffer.writeUtf(getObjectiveName());
                return new ClientboundSetDisplayObjectivePacket(buffer);
            }
        };
    }

    @Override
    public BlockDestructionPacket blockDestructionPacket(int id, BlockPosition position, int state) {
        return new BlockDestructionPacket(id, position, state) {
            @Override
            public ClientboundBlockDestructionPacket build() {
                return new ClientboundBlockDestructionPacket(getId(), wrap(getPosition()), getState());
            }
        };
    }

    @Override
    public SetExperiencePacket setExperiencePacket(float experienceProgress, int totalExperience, int experienceLevel) {
        return new SetExperiencePacket(experienceProgress, totalExperience, experienceLevel) {
            @Override
            public ClientboundSetExperiencePacket build() {
                return new ClientboundSetExperiencePacket(getExperienceProgress(), getTotalExperience(), getExperienceLevel());
            }
        };
    }

    @Override
    public StopSoundPacket stopSoundPacket(@Nullable NamespacedKey sound, @Nullable SoundCategory category) {
        return new StopSoundPacket(sound, category) {
            @Override
            public ClientboundStopSoundPacket build() {
                return new ClientboundStopSoundPacket(nullable(getSound()), nullable(getCategory()));
            }
        };
    }

    @Override
    public BossEventPacket bossEventPacket(BossEventPacket.Action action, BossBar bossBar) {
        return new BossEventPacket(action, bossBar) {
            @Override
            public ClientboundBossEventPacket build() {
                return switch (getAction()) {
                    case ADD -> ClientboundBossEventPacket.createAddPacket(((CraftBossBar) getBossBar()).getHandle());
                    case REMOVE -> ClientboundBossEventPacket.createRemovePacket(((CraftBossBar) getBossBar()).getHandle().getId());
                    case UPDATE_PCT -> ClientboundBossEventPacket.createUpdateProgressPacket(((CraftBossBar) getBossBar()).getHandle());
                    case UPDATE_NAME -> ClientboundBossEventPacket.createUpdateNamePacket(((CraftBossBar) getBossBar()).getHandle());
                    case UPDATE_STYLE -> ClientboundBossEventPacket.createUpdateStylePacket(((CraftBossBar) getBossBar()).getHandle());
                    case UPDATE_PROPERTIES -> ClientboundBossEventPacket.createUpdatePropertiesPacket(((CraftBossBar) getBossBar()).getHandle());
                };
            }
        };
    }

    @Override
    public SetCameraPacket setCameraPacket(int targetId) {
        return new SetCameraPacket(targetId) {
            @Override
            public ClientboundSetCameraPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getTargetId());
                return new ClientboundSetCameraPacket(buffer);
            }
        };
    }

    @Override
    public SystemChatPacket systemChatPacket(net.kyori.adventure.text.Component message, boolean overlay) {
        return new SystemChatPacket(message, overlay) {
            @Override
            public ClientboundSystemChatPacket build() {
                return new ClientboundSystemChatPacket(getMessage(), null, isOverlay());
            }
        };
    }

    @Override
    public ContainerClosePacket containerClosePacket(int windowId) {
        return new ContainerClosePacket(windowId) {
            @Override
            public ClientboundContainerClosePacket build() {
                return new ClientboundContainerClosePacket(getWindowId());
            }
        };
    }

    @Override
    public CooldownPacket cooldownPacket(Material item, int cooldown) {
        return new CooldownPacket(item, cooldown) {
            @Override
            public ClientboundCooldownPacket build() {
                return new ClientboundCooldownPacket(CraftMagicNumbers.getItem(getItem()), getCooldown());
            }
        };
    }

    @Override
    public CustomPayloadPacket customPayloadPacket(NamespacedKey channel, byte[]... bytes) {
        return new CustomPayloadPacket(channel, bytes) {
            @Override
            public ClientboundCustomPayloadPacket build() {
                return new ClientboundCustomPayloadPacket(wrap(getChannel()), new FriendlyByteBuf(Unpooled.wrappedBuffer(getBytes())));
            }
        };
    }

    @Override
    public AnimatePacket animatePacket(int entityId, AnimatePacket.Animation animation) {
        return new AnimatePacket(entityId, animation) {
            @Override
            public ClientboundAnimatePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeByte(getAnimation().getId());
                return new ClientboundAnimatePacket(buffer);
            }
        };
    }

    @Override
    public EntityAttachPacket entityAttachPacket(int holderId, int leashedId) {
        return new EntityAttachPacket(holderId, leashedId) {
            @Override
            public ClientboundSetEntityLinkPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeInt(getLeashedId());
                buffer.writeInt(getHolderId());
                return new ClientboundSetEntityLinkPacket(buffer);
            }
        };
    }

    @Override
    public RemoveEntitiesPacket removeEntitiesPacket(int... entityIds) {
        return new RemoveEntitiesPacket(entityIds) {
            @Override
            public ClientboundRemoveEntitiesPacket build() {
                return new ClientboundRemoveEntitiesPacket(getEntityIds());
            }
        };
    }

    @Override
    public SetEquipmentPacket setEquipmentPacket(int entityId, HashMap<SlotType, TNLItem> equipment) {
        return new SetEquipmentPacket(entityId, equipment) {
            @Override
            public ClientboundSetEquipmentPacket build() {
                List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new ArrayList<>();
                getEquipment().forEach((slot, itemStack) -> equipment.add(new Pair<>(wrap(slot), CraftItemStack.asNMSCopy(itemStack))));
                return new ClientboundSetEquipmentPacket(getEntityId(), equipment);
            }
        };
    }

    @Override
    public GameEventPacket gameEventPacket(GameEventPacket.Identifier identifier, float state) {
        return new GameEventPacket(identifier, state) {
            @Override
            public ClientboundGameEventPacket build() {
                return new ClientboundGameEventPacket(wrap(getIdentifier()), getState());
            }
        };
    }

    @Override
    public EntityStatusPacket entityStatusPacket(int entityId, EntityStatusPacket.Status status) {
        return new EntityStatusPacket(entityId, status) {
            @Override
            public ClientboundEntityEventPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeInt(getEntityId());
                buffer.writeByte(getStatus().getId());
                return new ClientboundEntityEventPacket(buffer);
            }
        };
    }

    @Override
    public AddEntityPacket addEntityPacket(int entityId, UUID uniqueId, Position position, EntityType entityType, int entityData, Vector velocity, double headYaw) {
        return new AddEntityPacket(entityId, uniqueId, position, entityType, entityData, velocity, headYaw) {
            @Override
            public ClientboundAddEntityPacket build() {
                return new ClientboundAddEntityPacket(getEntityId(), getUniqueId(), getPosition().getX(), getPosition().getY(),
                        getPosition().getZ(), getPosition().getPitch(), getPosition().getYaw(), wrap(getEntityType()),
                        getEntityData(), new Vec3(getVelocity().getX(), getVelocity().getY(), getVelocity().getZ()), getHeadYaw());
            }
        };
    }

    @Override
    public <W> EntityMetadataPacket<W> entityMetadataPacket(int entityId, W dataWatcher, boolean updateAll) {
        return new EntityMetadataPacket<>(entityId, dataWatcher, updateAll) {
            @Override
            public ClientboundSetEntityDataPacket build() {
                if (!(getMetadata() instanceof SynchedEntityData data)) throw new IllegalArgumentException();
                Int2ObjectMap<SynchedEntityData.DataItem<?>> items = Reflection.Field.getByType(data, Int2ObjectMap.class);
                List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();
                items.values().forEach(item -> list.add(item.value()));
                return new ClientboundSetEntityDataPacket(getEntityId(), list);
            }
        };
    }

    @Override
    public <W> EntityMetadataPacket<W> entityMetadataPacket(Entity entity, boolean updateAll) {
        return entityMetadataPacket(entity.getEntityId(), (W) ((CraftEntity) entity).getHandle().getEntityData(), updateAll);
    }

    @Override
    public EntityHeadRotationPacket entityHeadRotationPacket(int entityId, float yaw) {
        return new EntityHeadRotationPacket(entityId, yaw) {
            @Override
            public ClientboundRotateHeadPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeByte(Mth.floor(getYaw() * 256f / 360f));
                return new ClientboundRotateHeadPacket(buffer);
            }
        };
    }

    @Override
    public EntityBodyRotationPacket entityBodyRotationPacket(int entityId, float rotation) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeleportEntityPacket teleportEntityPacket(int entityId, Position position, boolean onGround) {
        return new TeleportEntityPacket(entityId, position, onGround) {
            @Override
            public ClientboundTeleportEntityPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeByte(Mth.floor(getPosition().getYaw() * 256.0F / 360.0F));
                buffer.writeByte(Mth.floor(getPosition().getPitch() * 256.0F / 360.0F));
                buffer.writeBoolean(isOnGround());
                return new ClientboundTeleportEntityPacket(buffer);
            }
        };
    }

    @Override
    public SetEntityMotionPacket setEntityMotionPacket(int entityId, Vector velocity) {
        return new SetEntityMotionPacket(entityId, velocity) {
            @Override
            public ClientboundSetEntityMotionPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeShort(getVelocity().getBlockX());
                buffer.writeShort(getVelocity().getBlockY());
                buffer.writeShort(getVelocity().getBlockZ());
                return new ClientboundSetEntityMotionPacket(buffer);
            }
        };
    }

    @Override
    public LivingEntitySpawnPacket livingEntitySpawnPacket(LivingEntity entity) {
        return new LivingEntitySpawnPacket(entity) {
            @Override
            public ClientboundAddEntityPacket build() {
                return new ClientboundAddEntityPacket(((CraftLivingEntity) getEntity()).getHandle());
            }
        };
    }

    @Override
    public MapChunkPacket mapChunkPacket(Chunk chunk, int section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SetPassengersPacket setPassengersPacket(int holderId, int[] passengers) {
        return new SetPassengersPacket(holderId, passengers) {
            @Override
            public ClientboundSetPassengersPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getHolderId());
                buffer.writeVarIntArray(getPassengers());
                return new ClientboundSetPassengersPacket(buffer);
            }
        };
    }

    @Override
    public AddPlayerPacket addPlayerPacket(int entityId, UUID uniqueId, Position position) {
        return new AddPlayerPacket(entityId, uniqueId, position) {
            @Override
            public ClientboundAddPlayerPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeUUID(getUniqueId());
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeByte((int) getPosition().getYaw());
                buffer.writeByte((int) getPosition().getPitch());
                return new ClientboundAddPlayerPacket(buffer);
            }
        };
    }

    @Override
    public OpenSignEditorPacket openSignEditorPacket(BlockPosition position) {
        return new OpenSignEditorPacket(position) {
            @Override
            public ClientboundOpenSignEditorPacket build() {
                return new ClientboundOpenSignEditorPacket(wrap(getPosition()));
            }
        };
    }

    @Override
    public OpenBookPacket openBookPacket(Hand hand) {
        return new OpenBookPacket(hand) {
            @Override
            public ClientboundOpenBookPacket build() {
                return new ClientboundOpenBookPacket(wrap(getHand()));
            }
        };
    }

    @Override
    public MoveVehiclePacket moveVehiclePacket(Position position) {
        return new MoveVehiclePacket(position) {
            @Override
            public ClientboundMoveVehiclePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeFloat(getPosition().getYaw());
                buffer.writeFloat(getPosition().getPitch());
                return new ClientboundMoveVehiclePacket(buffer);
            }
        };
    }

    @Override
    public OpenScreenPacket openScreenPacket(int containerId, OpenScreenPacket.Type type, net.kyori.adventure.text.Component title) {
        return new OpenScreenPacket(containerId, type, title) {
            @Override
            public ClientboundOpenScreenPacket build() {
                return new ClientboundOpenScreenPacket(getContainerId(), wrap(getType()), wrap(getTitle()));
            }
        };
    }

    @Override
    public PlayerInfoRemovePacket playerInfoRemovePacket(List<UUID> profileIds) {
        return new PlayerInfoRemovePacket(profileIds) {
            @Override
            public ClientboundPlayerInfoRemovePacket build() {
                return new ClientboundPlayerInfoRemovePacket(getProfileIds());
            }
        };
    }

    @Override
    public PlayerInfoUpdatePacket playerInfoUpdatePacket(List<PlayerInfoUpdatePacket.Action> actions, List<PlayerInfoUpdatePacket.Entry> entries) {
        return new PlayerInfoUpdatePacket(actions, entries) {
            @Override
            public ClientboundPlayerInfoUpdatePacket build() {
                List<ClientboundPlayerInfoUpdatePacket.Action> collection = new ArrayList<>();
                List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();
                getActions().forEach(action -> collection.add(wrap(action)));
                getEntries().forEach(entry -> entries.add(wrap(entry)));
                var packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.copyOf(collection), List.of());
                Reflection.Field.setByType(packet, List.class, entries);
                return packet;
            }
        };
    }

    @Override
    public ContainerSetSlotPacket containerSetSlotPacket(int containerId, int stateId, int slot, @Nullable ItemStack itemStack) {
        return new ContainerSetSlotPacket(containerId, stateId, slot, itemStack) {
            @Override
            public ClientboundContainerSetSlotPacket build() {
                return new ClientboundContainerSetSlotPacket(getContainerId(), getStateId(), getSlot(), CraftItemStack.asNMSCopy(getItemStack()));
            }
        };
    }

    @Override
    public SetTimePacket setTimePacket(long age, long timestamp, boolean cycle) {
        return new SetTimePacket(age, timestamp, cycle) {
            @Override
            public ClientboundSetTimePacket build() {
                return new ClientboundSetTimePacket(getAge(), getTimestamp(), isCycle());
            }
        };
    }

    @Override
    public ContainerSetDataPacket containerSetDataPacket(int containerId, int propertyId, int value) {
        return new ContainerSetDataPacket(containerId, propertyId, value) {
            @Override
            public ClientboundContainerSetDataPacket build() {
                return new ClientboundContainerSetDataPacket(getContainerId(), getPropertyId(), getValue());
            }
        };
    }

    @Override
    public ContainerSetContentPacket containerSetContentPacket(int containerId, int stateId, List<TNLItem> content, TNLItem cursor) {
        return new ContainerSetContentPacket(containerId, stateId, content, cursor) {
            @Override
            public ClientboundContainerSetContentPacket build() {
                NonNullList<net.minecraft.world.item.ItemStack> items = NonNullList.create();
                for (ItemStack item : getContent()) items.add(CraftItemStack.asNMSCopy(item));
                return new ClientboundContainerSetContentPacket(getContainerId(), getStateId(), items, wrap(getCursor()));
            }
        };
    }

    @Override
    public InitializeBorderPacket initializeBorderPacket(VirtualBorder virtualBorder) {
        return new InitializeBorderPacket(virtualBorder) {
            @Override
            public ClientboundInitializeBorderPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(virtualBorder.getCenter().x());
                buffer.writeDouble(virtualBorder.getCenter().z());
                buffer.writeDouble(virtualBorder.getOldSize());
                buffer.writeDouble(virtualBorder.getNewSize());
                buffer.writeVarLong(virtualBorder.getLerpTime());
                buffer.writeVarInt(VirtualBorder.MAX_SIZE);
                buffer.writeVarInt(virtualBorder.getWarningDistance());
                buffer.writeVarInt(virtualBorder.getWarningDelay());
                return new ClientboundInitializeBorderPacket(buffer);
            }
        };
    }

    @Override
    public SetBorderSizePacket setBorderSizePacket(double size) {
        return new SetBorderSizePacket(size) {
            @Override
            public ClientboundSetBorderSizePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(getSize());
                return new ClientboundSetBorderSizePacket(buffer);
            }
        };
    }

    @Override
    public SetBorderLerpSizePacket setBorderLerpSizePacket(double oldSize, double newSize, long lerpTime) {
        return new SetBorderLerpSizePacket(oldSize, newSize, lerpTime) {
            @Override
            public ClientboundSetBorderLerpSizePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(getOldSize());
                buffer.writeDouble(getNewSize());
                buffer.writeVarLong(getLerpTime());
                return new ClientboundSetBorderLerpSizePacket(buffer);
            }
        };
    }

    @Override
    public SetBorderCenterPacket setBorderCenterPacket(VirtualBorder.Center center) {
        return new SetBorderCenterPacket(center) {
            @Override
            public ClientboundSetBorderCenterPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(getCenter().x());
                buffer.writeDouble(getCenter().z());
                return new ClientboundSetBorderCenterPacket(buffer);
            }
        };
    }

    @Override
    public SetBorderWarningDelayPacket setBorderWarningDelayPacket(int warningDelay) {
        return new SetBorderWarningDelayPacket(warningDelay) {
            @Override
            public ClientboundSetBorderWarningDelayPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getWarningDelay());
                return new ClientboundSetBorderWarningDelayPacket(buffer);
            }
        };
    }

    @Override
    public SetBorderWarningDistancePacket setBorderWarningDistancePacket(int warningDistance) {
        return new SetBorderWarningDistancePacket(warningDistance) {
            @Override
            public ClientboundSetBorderWarningDistancePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getWarningDistance());
                return new ClientboundSetBorderWarningDistancePacket(buffer);
            }
        };
    }

    @Override
    public SelectAdvancementsTabPacket selectAdvancementsTabPacket(@Nullable NamespacedKey tab) {
        return new SelectAdvancementsTabPacket(tab) {
            @Override
            public ClientboundSelectAdvancementsTabPacket build() {
                return new ClientboundSelectAdvancementsTabPacket(nullable(tab));
            }
        };
    }

    @Override
    public HorseScreenOpenPacket horseScreenOpenPacket(int containerId, int size, int entityId) {
        return new HorseScreenOpenPacket(containerId, size, entityId) {
            @Override
            public ClientboundHorseScreenOpenPacket build() {
                return new ClientboundHorseScreenOpenPacket(getContainerId(), getSize(), getEntityId());
            }
        };
    }

    @Override
    public CommandSuggestionsPacket commandSuggestionsPacket(int id, CommandSuggestionsPacket.Suggestions suggestions) {
        return new CommandSuggestionsPacket(id, suggestions) {
            @Override
            public ClientboundCommandSuggestionsPacket build() {
                return new ClientboundCommandSuggestionsPacket(getId(), wrap(getSuggestions()));
            }
        };
    }

    @Override
    public ResourcePackPacket resourcePackPacket(String url, @Nullable String hash, @Nullable net.kyori.adventure.text.Component prompt, boolean required) {
        return new ResourcePackPacket(url, hash, prompt, required) {
            @Override
            public ClientboundResourcePackPacket build() {
                return new ClientboundResourcePackPacket(getUrl(), String.valueOf(getHash()), isRequired(), nullable(getPrompt()));
            }
        };
    }

    @Override
    public SetPlayerTeamPacket setPlayerTeamPacket(String name, SetPlayerTeamPacket.Option option, @Nullable SetPlayerTeamPacket.Parameters parameters, List<String> entries) {
        return new SetPlayerTeamPacket(name, option, parameters, entries) {
            @Override
            public ClientboundSetPlayerTeamPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeUtf(getName());
                buffer.writeByte(getOption().ordinal());
                if (getOption().needsParameters()) {
                    if (getParameters() == null) throw new IllegalStateException("parameters not present but required");
                    buffer.writeComponent(Component.literal(getParameters().getDisplayName()));
                    buffer.writeByte(getParameters().packOptions());
                    buffer.writeUtf(switch (getParameters().getNameTagVisibility()) {
                        case ALWAYS -> "always";
                        case NEVER -> "never";
                        case HIDE_FOR_OTHER_TEAMS -> "hideForOtherTeams";
                        case HIDE_FOR_OWN_TEAM -> "hideForOwnTeam";
                    });
                    buffer.writeUtf(switch (getParameters().getCollisionRule()) {
                        case ALWAYS -> "always";
                        case NEVER -> "never";
                        case PUSH_OTHER_TEAMS -> "pushOtherTeams";
                        case PUSH_OWN_TEAM -> "pushOwnTeam";
                    });
                    buffer.writeEnum(wrap(getParameters().getColor()));
                    buffer.writeComponent(Component.literal(getParameters().getPrefix()));
                    buffer.writeComponent(Component.literal(getParameters().getSuffix()));
                }
                if (getOption().needsEntries()) buffer.writeCollection(getEntries(), FriendlyByteBuf::writeUtf);
                return new ClientboundSetPlayerTeamPacket(buffer);
            }
        };
    }

    @Override
    public TagQueryPacket tagQueryPacket(int transactionId, @Nullable NBTTag tag) {
        return new TagQueryPacket(transactionId, tag) {
            @Override
            public ClientboundTagQueryPacket build() {
                return new ClientboundTagQueryPacket(getTransactionId(), getTag() != null ? getTag().versioned() : null);
            }
        };
    }

    @Override
    public SetChunkCacheRadiusPacket setChunkCacheRadiusPacket(int radius) {
        return new SetChunkCacheRadiusPacket(radius) {
            @Override
            public ClientboundSetChunkCacheRadiusPacket build() {
                return new ClientboundSetChunkCacheRadiusPacket(getRadius());
            }
        };
    }

    @Override
    public RotateHeadPacket rotateHeadPacket(int entityId, byte yaw) {
        return new RotateHeadPacket(entityId, yaw) {
            @Override
            public ClientboundRotateHeadPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeByte(getYaw());
                return new ClientboundRotateHeadPacket(buffer);
            }
        };
    }

    @Override
    public TakeItemEntityPacket takeItemEntityPacket(int entityId, int playerId, int amount) {
        return new TakeItemEntityPacket(entityId, playerId, amount) {
            @Override
            public ClientboundTakeItemEntityPacket build() {
                return new ClientboundTakeItemEntityPacket(getEntityId(), getPlayerId(), getAmount());
            }
        };
    }

    @Override
    public SetChunkCacheCenterPacket setChunkCacheCenterPacket(int x, int z) {
        return new SetChunkCacheCenterPacket(x, z) {
            @Override
            public ClientboundSetChunkCacheCenterPacket build() {
                return new ClientboundSetChunkCacheCenterPacket(getX(), getZ());
            }
        };
    }

    @Override
    public ChangeDifficultyPacket changeDifficultyPacket(Difficulty difficulty, boolean locked) {
        return new ChangeDifficultyPacket(difficulty, locked) {
            @Override
            public ClientboundChangeDifficultyPacket build() {
                return new ClientboundChangeDifficultyPacket(wrap(getDifficulty()), isLocked());
            }
        };
    }

    @Override
    public KeepAlivePacket keepAlivePacket(long id) {
        return new KeepAlivePacket(id) {
            @Override
            public ClientboundKeepAlivePacket build() {
                return new ClientboundKeepAlivePacket(getId());
            }
        };
    }

    @Override
    public SetActionBarTextPacket setActionBarTextPacket(net.kyori.adventure.text.Component text) {
        return new SetActionBarTextPacket(text) {
            @Override
            public ClientboundSetActionBarTextPacket build() {
                return new ClientboundSetActionBarTextPacket(wrap(getText()));
            }
        };
    }

    @Override
    public DisconnectPacket disconnectPacket(net.kyori.adventure.text.Component reason) {
        return new DisconnectPacket(reason) {
            @Override
            public ClientboundDisconnectPacket build() {
                return new ClientboundDisconnectPacket(wrap(getReason()));
            }
        };
    }

    @Override
    public ForgetLevelChunkPacket forgetLevelChunkPacket(int x, int z) {
        return new ForgetLevelChunkPacket(x, z) {
            @Override
            public ClientboundForgetLevelChunkPacket build() {
                return new ClientboundForgetLevelChunkPacket(getX(), getZ());
            }
        };
    }

    @Override
    public TabListPacket tabListPacket(net.kyori.adventure.text.Component header, net.kyori.adventure.text.Component footer) {
        return new TabListPacket(header, footer) {
            @Override
            public ClientboundTabListPacket build() {
                ClientboundTabListPacket packet = new ClientboundTabListPacket(wrap(getHeader()), wrap(getFooter()));
                packet.adventure$header = getHeader();
                packet.adventure$footer = getFooter();
                return packet;
            }
        };
    }

    @Override
    public PingPacket pingPacket(int id) {
        return new PingPacket(id) {
            @Override
            public ClientboundPingPacket build() {
                return new ClientboundPingPacket(getId());
            }
        };
    }

    @Override
    public BlockChangedAckPacket blockChangedAckPacket(int sequence) {
        return new BlockChangedAckPacket(sequence) {
            @Override
            public ClientboundBlockChangedAckPacket build() {
                return new ClientboundBlockChangedAckPacket(getSequence());
            }
        };
    }

    @Override
    public TitlePacket.SetTitlesAnimation setTitlesAnimation(int timeIn, int timeStay, int timeOut) {
        return new TitlePacket.SetTitlesAnimation(timeIn, timeStay, timeOut) {
            @Override
            public ClientboundSetTitlesAnimationPacket build() {
                return new ClientboundSetTitlesAnimationPacket(getTimeIn(), getTimeStay(), getTimeOut());
            }
        };
    }

    @Override
    public TitlePacket.SetTitleText setTitleText(net.kyori.adventure.text.Component text) {
        return new TitlePacket.SetTitleText(text) {
            @Override
            public ClientboundSetTitleTextPacket build() {
                return new ClientboundSetTitleTextPacket(wrap(getText()));
            }
        };
    }

    @Override
    public TitlePacket.SetSubtitleText setSubtitleText(net.kyori.adventure.text.Component text) {
        return new TitlePacket.SetSubtitleText(text) {
            @Override
            public ClientboundSetSubtitleTextPacket build() {
                return new ClientboundSetSubtitleTextPacket(wrap(getText()));
            }
        };
    }

    @Override
    public TitlePacket.ClearTitles clearTitles(boolean resetTimes) {
        return new TitlePacket.ClearTitles(resetTimes) {
            @Override
            public ClientboundClearTitlesPacket build() {
                return new ClientboundClearTitlesPacket(isResetTimes());
            }
        };
    }

    @Override
    public SetEntityLinkPacket setEntityLinkPacket(int leashHolderId, int leashedEntityId) {
        return new SetEntityLinkPacket(leashHolderId, leashedEntityId) {
            @Override
            public ClientboundSetEntityLinkPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeInt(getLeashHolderId());
                buffer.writeInt(getLeashedEntityId());
                return new ClientboundSetEntityLinkPacket(buffer);
            }
        };
    }

    @Override
    public BlockEventPacket blockEventPacket(BlockPosition position, Material blockType, int type, int data) {
        return new BlockEventPacket(position, blockType, type, data) {
            @Override
            public ClientboundBlockEventPacket build() {
                return new ClientboundBlockEventPacket(wrap(getPosition()), CraftMagicNumbers.getBlock(getBlockType()), getType(), getData());
            }
        };
    }

    @Override
    public SetDefaultSpawnPositionPacket setDefaultSpawnPositionPacket(BlockPosition position, float angle) {
        return new SetDefaultSpawnPositionPacket(position, angle) {
            @Override
            public ClientboundSetDefaultSpawnPositionPacket build() {
                return new ClientboundSetDefaultSpawnPositionPacket(wrap(getPosition()), getAngle());
            }
        };
    }

    @Override
    public SetScorePacket setScorePacket(SetScorePacket.Method method, @Nullable String objectiveName, String owner, int score) {
        return new SetScorePacket(method, objectiveName, owner, score) {
            @Override
            public ClientboundSetScorePacket build() {
                return new ClientboundSetScorePacket(switch (getMethod()) {
                    case REMOVE -> ServerScoreboard.Method.REMOVE;
                    case UPDATE -> ServerScoreboard.Method.CHANGE;
                }, getObjectiveName(), getOwner(), getScore());
            }
        };
    }

    @Override
    public UpdateAdvancementsPacket updateAdvancementsPacket(boolean reset, HashMap<NamespacedKey, Advancement.Builder> added, List<NamespacedKey> removed, HashMap<NamespacedKey, Advancement.Progress> progress) {
        return new UpdateAdvancementsPacket(reset, added, removed, progress) {
            @Override
            public ClientboundUpdateAdvancementsPacket build() {
                Collection<net.minecraft.advancements.Advancement> added = new ArrayList<>();
                getAdded().forEach((key, builder) -> added.add(wrap(builder.build())));
                Set<ResourceLocation> removed = new HashSet<>();
                getRemoved().forEach(key -> removed.add(wrap(key)));
                Map<ResourceLocation, AdvancementProgress> progress = new HashMap<>();
                getProgress().forEach((key, advancementProgress) -> progress.put(wrap(key), wrap(advancementProgress)));
                return new ClientboundUpdateAdvancementsPacket(isReset(), added, removed, progress);
            }
        };
    }

    @Override
    public LevelEventPacket levelEventPacket(int eventId, BlockPosition position, int data, boolean global) {
        return new LevelEventPacket(eventId, position, data, global) {
            @Override
            public ClientboundLevelEventPacket build() {
                return new ClientboundLevelEventPacket(getEventId(), wrap(getPosition()), getData(), isGlobal());
            }
        };
    }

    @Override
    public PlayerChatPacket playerChatPacket(UUID sender, int index, @Nullable net.nonswag.tnl.listener.api.chat.MessageSignature signature, SignedMessageBody body, @Nullable net.kyori.adventure.text.Component unsignedContent, Filter filter, ChatType chatType) {
        return new PlayerChatPacket(sender, index, signature, body, unsignedContent, filter, chatType) {
            @Override
            public ClientboundPlayerChatPacket build() {
                return new ClientboundPlayerChatPacket(getSender(), getIndex(), nullable(getSignature()), wrap(getBody()), nullable(getUnsignedContent()), wrap(getFilter()), wrap(getChatType()));
            }
        };
    }

    @Override
    public SetHealthPacket setHealthPacket(float health, int food, float saturation) {
        return new SetHealthPacket(health, food, saturation) {
            @Override
            public ClientboundSetHealthPacket build() {
                return new ClientboundSetHealthPacket(getHealth(), getFood(), getSaturation());
            }
        };
    }

    @Override
    public ServerDataPacket serverDataPacket(@Nullable net.kyori.adventure.text.Component motd, @Nullable String serverIcon, boolean chatPreview) {
        return new ServerDataPacket(motd, serverIcon, chatPreview) {
            @Override
            public ClientboundServerDataPacket build() {
                return new ClientboundServerDataPacket(nullable(getMotd()), getServerIcon(), isChatPreview());
            }
        };
    }

    @Override
    public SectionBlocksUpdatePacket sectionBlocksUpdatePacket(long section, short[] positions, int[] states, boolean suppressLightUpdates) {
        return new SectionBlocksUpdatePacket(section, positions, states, suppressLightUpdates) {
            @Override
            public ClientboundSectionBlocksUpdatePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeLong(getSection());
                buffer.writeBoolean(isSuppressLightUpdates());
                buffer.writeVarInt(getPositions().length);
                for (int i = 0; i < getPositions().length; ++i) {
                    buffer.writeVarLong(((long) getStates()[i]) << 12L | getPositions()[i]);
                }
                return new ClientboundSectionBlocksUpdatePacket(buffer);
            }
        };
    }

    @Override
    public PlayerLookAtPacket playerLookAtPacket(PlayerLookAtPacket.Anchor self, Position position, int entityId, @Nullable PlayerLookAtPacket.Anchor target) {
        return new PlayerLookAtPacket(self, position, entityId, target) {
            @Override
            public ClientboundPlayerLookAtPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeEnum(wrap(getSelf()));
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeBoolean(getTarget() != null);
                if (getTarget() != null) {
                    buffer.writeVarInt(getEntityId());
                    buffer.writeEnum(wrap(getTarget()));
                }
                return new ClientboundPlayerLookAtPacket(buffer);
            }
        };
    }

    @Override
    public PlayerCombatKillPacket playerCombatKillPacket(int victimId, int killerId, net.kyori.adventure.text.Component message) {
        return new PlayerCombatKillPacket(victimId, killerId, message) {
            @Override
            public ClientboundPlayerCombatKillPacket build() {
                return new ClientboundPlayerCombatKillPacket(getVictimId(), getKillerId(), wrap(getMessage()));
            }
        };
    }

    @Override
    public PlayerCombatEndPacket playerCombatEndPacket(int durationSinceLastAttack, int killerId) {
        return new PlayerCombatEndPacket(durationSinceLastAttack, killerId) {
            @Override
            public ClientboundPlayerCombatEndPacket build() {
                return new ClientboundPlayerCombatEndPacket(getKillerId(), getDurationSinceLastAttack());
            }
        };
    }

    @Override
    public PlayerCombatEnterPacket playerCombatEnterPacket() {
        return new PlayerCombatEnterPacket() {
            @Override
            public ClientboundPlayerCombatEnterPacket build() {
                return new ClientboundPlayerCombatEnterPacket();
            }
        };
    }

    @Override
    public MerchantOffersPacket merchantOffersPacket(int containerId, List<MerchantOffersPacket.Offer> offers, int level, int experience, boolean showProgress, boolean canRestock) {
        return new MerchantOffersPacket(containerId, offers, level, experience, showProgress, canRestock) {
            @Override
            public ClientboundMerchantOffersPacket build() {
                MerchantOffers offers = new MerchantOffers();
                getOffers().forEach(offer -> offers.add(wrap(offer)));
                return new ClientboundMerchantOffersPacket(getContainerId(), offers, getLevel(), getExperience(), showProgress(), canRestock());
            }
        };
    }

    @Override
    public AddExperienceOrbPacket addExperienceOrbPacket(int entityId, Position position, int value) {
        return new AddExperienceOrbPacket(entityId, position, value) {
            @Override
            public ClientboundAddExperienceOrbPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeShort(getValue());
                return new ClientboundAddExperienceOrbPacket(buffer);
            }
        };
    }

    @Override
    public ExplodePacket explodePacket(Position position, float radius, List<BlockPosition> affectedBlocks, Vector knockback) {
        return new ExplodePacket(position, radius, affectedBlocks, knockback) {
            @Override
            public ClientboundExplodePacket build() {
                List<BlockPos> affectedBlocks = new ArrayList<>();
                getAffectedBlocks().forEach(position -> affectedBlocks.add(wrap(position)));
                return new ClientboundExplodePacket(getPosition().getX(), getPosition().getY(), getPosition().getZ(),
                        getRadius(), affectedBlocks, new Vec3(getKnockback().getX(), getKnockback().getY(), getKnockback().getZ()));
            }
        };
    }

    @Override
    public PlaceGhostRecipePacket placeGhostRecipePacket(int containerId, NamespacedKey recipe) {
        return new PlaceGhostRecipePacket(containerId, recipe) {
            @Override
            public ClientboundPlaceGhostRecipePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeByte(getContainerId());
                buffer.writeResourceLocation(wrap(getRecipe()));
                return new ClientboundPlaceGhostRecipePacket(buffer);
            }
        };
    }

    @Override
    public BlockUpdatePacket blockUpdatePacket(BlockPosition position, int blockState) {
        return new BlockUpdatePacket(position, blockState) {
            @Override
            public ClientboundBlockUpdatePacket build() {
                return new ClientboundBlockUpdatePacket(wrap(getPosition()), Block.stateById(getBlockState()));
            }
        };
    }

    @Override
    public CustomChatCompletionsPacket customChatCompletionsPacket(CustomChatCompletionsPacket.Action action, List<String> entries) {
        return new CustomChatCompletionsPacket(action, entries) {
            @Override
            public ClientboundCustomChatCompletionsPacket build() {
                return new ClientboundCustomChatCompletionsPacket(switch (getAction()) {
                    case ADD -> ClientboundCustomChatCompletionsPacket.Action.ADD;
                    case REMOVE -> ClientboundCustomChatCompletionsPacket.Action.REMOVE;
                    case SET -> ClientboundCustomChatCompletionsPacket.Action.SET;
                }, getEntries());
            }
        };
    }

    @Override
    public EntityEventPacket entityEventPacket(int entityId, byte eventId) {
        return new EntityEventPacket(entityId, eventId) {
            @Override
            public ClientboundEntityEventPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeInt(getEntityId());
                buffer.writeByte(getEventId());
                return new ClientboundEntityEventPacket(buffer);
            }
        };
    }

    @Override
    public MoveEntityPacket.PositionRotation moveEntityPacket(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new MoveEntityPacket.PositionRotation(entityId, x, y, z, yaw, pitch, onGround) {
            @Override
            public ClientboundMoveEntityPacket.PosRot build() {
                return new ClientboundMoveEntityPacket.PosRot(getEntityId(), (short) getX(), (short) getY(), (short) getZ(), (byte) getYaw(), (byte) getPitch(), isOnGround());
            }
        };
    }

    @Override
    public MoveEntityPacket.Rotation moveEntityPacket(int entityId, float yaw, float pitch, boolean onGround) {
        return new MoveEntityPacket.Rotation(entityId, yaw, pitch, onGround) {
            @Override
            public ClientboundMoveEntityPacket.Rot build() {
                return new ClientboundMoveEntityPacket.Rot(getEntityId(), (byte) getYaw(), (byte) getPitch(), isOnGround());
            }
        };
    }

    @Override
    public MoveEntityPacket.Position moveEntityPacket(int entityId, double x, double y, double z, boolean onGround) {
        return new MoveEntityPacket.Position(entityId, x, y, z, onGround) {
            @Override
            public ClientboundMoveEntityPacket.Pos build() {
                return new ClientboundMoveEntityPacket.Pos(getEntityId(), (short) getX(), (short) getY(), (short) getZ(), isOnGround());
            }
        };
    }

    @Override
    public DisguisedChatPacket disguisedChatPacket(net.kyori.adventure.text.Component message, ChatType chatType) {
        return new DisguisedChatPacket(message, chatType) {
            @Override
            public ClientboundDisguisedChatPacket build() {
                return new ClientboundDisguisedChatPacket(wrap(getMessage()), wrap(getChatType()));
            }
        };
    }

    private final HashMap<Class<? extends Packet<ClientGamePacketListener>>, Class<? extends PacketBuilder>> PACKET_MAP = new HashMap<>() {{
        put(ClientboundAddEntityPacket.class, AddEntityPacket.class);
        put(ClientboundAddExperienceOrbPacket.class, AddExperienceOrbPacket.class);
        put(ClientboundAddPlayerPacket.class, AddPlayerPacket.class);
        put(ClientboundAnimatePacket.class, AnimatePacket.class);
        put(ClientboundAwardStatsPacket.class, AwardStatsPacket.class);
        put(ClientboundBlockChangedAckPacket.class, BlockChangedAckPacket.class);
        put(ClientboundBlockDestructionPacket.class, BlockDestructionPacket.class);
        put(ClientboundBlockEntityDataPacket.class, BlockEntityDataPacket.class);
        put(ClientboundBlockEventPacket.class, BlockEventPacket.class);
        put(ClientboundBlockUpdatePacket.class, BlockUpdatePacket.class);
        put(ClientboundBossEventPacket.class, BossEventPacket.class);
        put(ClientboundChangeDifficultyPacket.class, ChangeDifficultyPacket.class);
        put(ClientboundClearTitlesPacket.class, TitlePacket.ClearTitles.class);
        put(ClientboundCommandsPacket.class, CommandsPacket.class);
        put(ClientboundCommandSuggestionsPacket.class, CommandSuggestionsPacket.class);
        put(ClientboundContainerClosePacket.class, ContainerClosePacket.class);
        put(ClientboundContainerSetContentPacket.class, ContainerSetContentPacket.class);
        put(ClientboundContainerSetDataPacket.class, ContainerSetDataPacket.class);
        put(ClientboundContainerSetSlotPacket.class, ContainerSetSlotPacket.class);
        put(ClientboundCooldownPacket.class, CooldownPacket.class);
        put(ClientboundCustomChatCompletionsPacket.class, CustomChatCompletionsPacket.class);
        put(ClientboundCustomPayloadPacket.class, CustomPayloadPacket.class);
        put(ClientboundDeleteChatPacket.class, DeleteChatPacket.class);
        put(ClientboundDisconnectPacket.class, DisconnectPacket.class);
        put(ClientboundDisguisedChatPacket.class, DisguisedChatPacket.class);
        put(ClientboundEntityEventPacket.class, EntityEventPacket.class);
        put(ClientboundExplodePacket.class, ExplodePacket.class);
        put(ClientboundForgetLevelChunkPacket.class, ForgetLevelChunkPacket.class);
        put(ClientboundGameEventPacket.class, GameEventPacket.class);
        put(ClientboundHorseScreenOpenPacket.class, HorseScreenOpenPacket.class);
        put(ClientboundInitializeBorderPacket.class, InitializeBorderPacket.class);
        put(ClientboundKeepAlivePacket.class, KeepAlivePacket.class);
        put(ClientboundLevelChunkWithLightPacket.class, LevelChunkWithLightPacket.class);
        put(ClientboundLevelEventPacket.class, LevelEventPacket.class);
        put(ClientboundLevelParticlesPacket.class, LevelParticlesPacket.class);
        put(ClientboundLightUpdatePacket.class, LightUpdatePacket.class);
        put(ClientboundLoginPacket.class, LoginPacket.class);
        put(ClientboundMapItemDataPacket.class, MapItemDataPacket.class);
        put(ClientboundMerchantOffersPacket.class, MerchantOffersPacket.class);
        put(ClientboundMoveEntityPacket.class, MoveEntityPacket.class);
        put(ClientboundMoveEntityPacket.Pos.class, MoveEntityPacket.Position.class);
        put(ClientboundMoveEntityPacket.Rot.class, MoveEntityPacket.Rotation.class);
        put(ClientboundMoveEntityPacket.PosRot.class, MoveEntityPacket.PositionRotation.class);
        put(ClientboundMoveVehiclePacket.class, MoveVehiclePacket.class);
        put(ClientboundOpenBookPacket.class, OpenBookPacket.class);
        put(ClientboundOpenScreenPacket.class, OpenScreenPacket.class);
        put(ClientboundOpenSignEditorPacket.class, OpenSignEditorPacket.class);
        put(ClientboundPingPacket.class, PingPacket.class);
        put(ClientboundPlaceGhostRecipePacket.class, PlaceGhostRecipePacket.class);
        put(ClientboundPlayerAbilitiesPacket.class, PlayerAbilitiesPacket.class);
        put(ClientboundPlayerChatPacket.class, PlayerChatPacket.class);
        put(ClientboundPlayerCombatEndPacket.class, PlayerCombatEndPacket.class);
        put(ClientboundPlayerCombatEnterPacket.class, PlayerCombatEnterPacket.class);
        put(ClientboundPlayerCombatKillPacket.class, PlayerCombatKillPacket.class);
        put(ClientboundPlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket.class);
        put(ClientboundPlayerInfoRemovePacket.class, PlayerInfoRemovePacket.class);
        put(ClientboundPlayerLookAtPacket.class, PlayerLookAtPacket.class);
        put(ClientboundPlayerPositionPacket.class, PlayerPositionPacket.class);
        put(ClientboundRecipePacket.class, RecipePacket.class);
        put(ClientboundRemoveEntitiesPacket.class, RemoveEntitiesPacket.class);
        put(ClientboundRemoveMobEffectPacket.class, RemoveMobEffectPacket.class);
        put(ClientboundResourcePackPacket.class, ResourcePackPacket.class);
        put(ClientboundRespawnPacket.class, RespawnPacket.class);
        put(ClientboundRotateHeadPacket.class, RotateHeadPacket.class);
        put(ClientboundSectionBlocksUpdatePacket.class, SectionBlocksUpdatePacket.class);
        put(ClientboundSelectAdvancementsTabPacket.class, SelectAdvancementsTabPacket.class);
        put(ClientboundServerDataPacket.class, ServerDataPacket.class);
        put(ClientboundSetActionBarTextPacket.class, SetActionBarTextPacket.class);
        put(ClientboundSetBorderCenterPacket.class, SetBorderCenterPacket.class);
        put(ClientboundSetBorderLerpSizePacket.class, SetBorderLerpSizePacket.class);
        put(ClientboundSetBorderSizePacket.class, SetBorderSizePacket.class);
        put(ClientboundSetBorderWarningDelayPacket.class, SetBorderWarningDelayPacket.class);
        put(ClientboundSetBorderWarningDistancePacket.class, SetBorderWarningDistancePacket.class);
        put(ClientboundSetCameraPacket.class, SetCameraPacket.class);
        put(ClientboundSetCarriedItemPacket.class, SetCarriedItemPacket.class);
        put(ClientboundSetChunkCacheCenterPacket.class, SetChunkCacheCenterPacket.class);
        put(ClientboundSetChunkCacheRadiusPacket.class, SetChunkCacheRadiusPacket.class);
        put(ClientboundSetDefaultSpawnPositionPacket.class, SetDefaultSpawnPositionPacket.class);
        put(ClientboundSetDisplayObjectivePacket.class, SetDisplayObjectivePacket.class);
        put(ClientboundSetEntityDataPacket.class, SetEntityDataPacket.class);
        put(ClientboundSetEntityLinkPacket.class, SetEntityLinkPacket.class);
        put(ClientboundSetEntityMotionPacket.class, SetEntityMotionPacket.class);
        put(ClientboundSetEquipmentPacket.class, SetEquipmentPacket.class);
        put(ClientboundSetExperiencePacket.class, SetExperiencePacket.class);
        put(ClientboundSetHealthPacket.class, SetHealthPacket.class);
        put(ClientboundSetObjectivePacket.class, SetObjectivePacket.class);
        put(ClientboundSetPassengersPacket.class, SetPassengersPacket.class);
        put(ClientboundSetPlayerTeamPacket.class, SetPlayerTeamPacket.class);
        put(ClientboundSetScorePacket.class, SetScorePacket.class);
        put(ClientboundSetSimulationDistancePacket.class, SetSimulationDistancePacket.class);
        put(ClientboundSetSubtitleTextPacket.class, TitlePacket.SetSubtitleText.class);
        put(ClientboundSetTimePacket.class, SetTimePacket.class);
        put(ClientboundSetTitlesAnimationPacket.class, TitlePacket.SetTitlesAnimation.class);
        put(ClientboundSetTitleTextPacket.class, TitlePacket.SetTitleText.class);
        put(ClientboundSoundEntityPacket.class, SoundEntityPacket.class);
        put(ClientboundSoundPacket.class, SoundPacket.class);
        put(ClientboundStopSoundPacket.class, StopSoundPacket.class);
        put(ClientboundSystemChatPacket.class, SystemChatPacket.class);
        put(ClientboundTabListPacket.class, TabListPacket.class);
        put(ClientboundTagQueryPacket.class, TagQueryPacket.class);
        put(ClientboundTakeItemEntityPacket.class, TakeItemEntityPacket.class);
        put(ClientboundTeleportEntityPacket.class, TeleportEntityPacket.class);
        put(ClientboundUpdateAdvancementsPacket.class, UpdateAdvancementsPacket.class);
        put(ClientboundUpdateAttributesPacket.class, UpdateAttributesPacket.class);
        put(ClientboundUpdateMobEffectPacket.class, UpdateMobEffectPacket.class);
        put(ClientboundUpdateRecipesPacket.class, UpdateRecipesPacket.class);
        put(ClientboundUpdateTagsPacket.class, UpdateTagsPacket.class);
    }};

    @Override
    public <P> Class<? extends PacketBuilder> map(Class<P> clazz) {
        Class<? extends PacketBuilder> aClass = PACKET_MAP.get(clazz);
        if (aClass != null) return aClass;
        throw new IllegalStateException("Unmapped outgoing packet: " + clazz.getName());
    }

    @Override
    public <P> PacketBuilder map(P packet) {
        Function<P, PacketBuilder> original = p -> new PacketBuilder() {
            @Override
            public P build() {
                return packet;
            }
        };
        if (packet instanceof ClientboundInitializeBorderPacket instance) {
            VirtualBorder border = new VirtualBorder(new VirtualBorder.Center(instance.getNewCenterX(), instance.getNewCenterZ()));
            border.setOldSize(instance.getOldSize());
            border.setNewSize(instance.getNewSize());
            border.setLerpTime(instance.getLerpTime());
            border.setWarningDistance(instance.getWarningBlocks());
            border.setWarningDelay(instance.getWarningTime());
            return InitializeBorderPacket.create(border);
        } else if (packet instanceof ClientboundAnimatePacket instance) {
            return AnimatePacket.create(instance.getId(), AnimatePacket.Animation.values()[instance.getAction()]);
        } else if (packet instanceof ClientboundSetExperiencePacket instance) {
            return SetExperiencePacket.create(instance.getExperienceProgress(), instance.getTotalExperience(), instance.getExperienceLevel());
        } else if (packet instanceof ClientboundCommandSuggestionsPacket instance) {
            return CommandSuggestionsPacket.create(instance.getId(), wrap(instance.getSuggestions()));
        } else if (packet instanceof ClientboundSelectAdvancementsTabPacket instance) {
            return SelectAdvancementsTabPacket.create(nullable(instance.getTab()));
        } else if (packet instanceof ClientboundHorseScreenOpenPacket instance) {
            return HorseScreenOpenPacket.create(instance.getContainerId(), instance.getSize(), instance.getEntityId());
        } else if (packet instanceof ClientboundMoveVehiclePacket instance) {
            return MoveVehiclePacket.create(new Position(instance.getX(), instance.getY(), instance.getZ(), instance.getYRot(), instance.getXRot()));
        } else if (packet instanceof ClientboundSetCameraPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            return SetCameraPacket.create(buffer.readVarInt());
        } else if (packet instanceof ClientboundGameEventPacket instance) {
            return GameEventPacket.create(wrap(instance.getEvent()), instance.getParam());
        } else if (packet instanceof ClientboundStopSoundPacket instance) {
            return StopSoundPacket.create(nullable(instance.getName()), nullable(instance.getSource()));
        } else if (packet instanceof ClientboundOpenBookPacket instance) {
            return OpenBookPacket.create(wrap(instance.getHand()));
        } else if (packet instanceof ClientboundLightUpdatePacket instance) {
            return original.apply(packet);
        } else if (packet instanceof ClientboundSetCarriedItemPacket instance) {
            return SetCarriedItemPacket.create(instance.getSlot());
        } else if (packet instanceof ClientboundSetDisplayObjectivePacket instance) {
            return SetDisplayObjectivePacket.create(instance.getSlot(), instance.getObjectiveName());
        } else if (packet instanceof ClientboundSetTimePacket instance) {
            return SetTimePacket.create(instance.getGameTime(), instance.getDayTime(), instance.getDayTime() < 0);
        } else if (packet instanceof ClientboundContainerSetContentPacket instance) {
            return ContainerSetContentPacket.create(instance.getContainerId(), instance.getStateId(), wrap(instance.getItems(), 0), wrap(instance.getCarriedItem()));
        } else if (packet instanceof ClientboundSetPlayerTeamPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            String name = buffer.readUtf();
            SetPlayerTeamPacket.Option option = SetPlayerTeamPacket.Option.values()[buffer.readByte()];
            SetPlayerTeamPacket.Parameters parameters = null;
            if (option.needsParameters()) parameters = wrap(new ClientboundSetPlayerTeamPacket.Parameters(buffer));
            List<String> entries = new ArrayList<>();
            if (option.needsEntries()) entries = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
            return SetPlayerTeamPacket.create(name, option, parameters, entries);
        } else if (packet instanceof ClientboundDisguisedChatPacket instance) {
            return DisguisedChatPacket.create(wrap(instance.message()), wrap(instance.chatType()));
        } else if (packet instanceof ClientboundUpdateTagsPacket instance) {
            return new UpdateTagsPacket() {
                @Override
                public ClientboundUpdateTagsPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundSetSimulationDistancePacket instance) {
            return SetSimulationDistancePacket.create(instance.simulationDistance());
        } else if (packet instanceof ClientboundLevelChunkPacketData instance) {
            return original.apply(packet);
        } else if (packet instanceof ClientboundTagQueryPacket instance) {
            return TagQueryPacket.create(instance.getTransactionId(), nullable(instance.getTag()));
        } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket instance) {
            return SetChunkCacheRadiusPacket.create(instance.getRadius());
        } else if (packet instanceof ClientboundRotateHeadPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            return RotateHeadPacket.create(buffer.readVarInt(), buffer.readByte());
        } else if (packet instanceof ClientboundLoginPacket instance) {
            return new LoginPacket() {
                @Override
                public ClientboundLoginPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundLightUpdatePacketData instance) {
            return original.apply(packet);
        } else if (packet instanceof ClientboundTakeItemEntityPacket instance) {
            return TakeItemEntityPacket.create(instance.getItemId(), instance.getPlayerId(), instance.getAmount());
        } else if (packet instanceof ClientboundSetChunkCacheCenterPacket instance) {
            return SetChunkCacheCenterPacket.create(instance.getX(), instance.getZ());
        } else if (packet instanceof ClientboundCustomPayloadPacket instance) {
            return CustomPayloadPacket.create(wrap(instance.getIdentifier()), wrap(instance.getData()));
        } else if (packet instanceof ClientboundSectionBlocksUpdatePacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            long section = buffer.readLong();
            boolean suppressLightUpdates = buffer.readBoolean();
            short[] positions = new short[buffer.readVarInt()];
            int[] states = new int[positions.length];
            for (int i = 0; i < positions.length; ++i) {
                long id = buffer.readVarLong();
                positions[i] = (short) ((int) (id & 4095L));
                states[i] = (int) (id >>> 12);
            }
            return SectionBlocksUpdatePacket.create(section, positions, states, suppressLightUpdates);
        } else if (packet instanceof ClientboundBlockDestructionPacket instance) {
            return BlockDestructionPacket.create(instance.getId(), wrap(instance.getPos()), instance.getProgress());
        } else if (packet instanceof ClientboundUpdateRecipesPacket instance) {
            return new UpdateRecipesPacket() {
                @Override
                public ClientboundUpdateRecipesPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundDisconnectPacket instance) {
            return DisconnectPacket.create(wrap(instance.getReason()));
        } else if (packet instanceof ClientboundSoundEntityPacket instance) {
            return new SoundEntityPacket() {
                @Override
                public ClientboundSoundEntityPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPingPacket instance) {
            return PingPacket.create(instance.getId());
        } else if (packet instanceof ClientboundSetEntityDataPacket instance) {
            return new SetEntityDataPacket() {
                @Override
                public ClientboundSetEntityDataPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundOpenSignEditorPacket instance) {
            return OpenSignEditorPacket.create(wrap(instance.getPos()));
        } else if (packet instanceof ClientboundBlockChangedAckPacket instance) {
            return BlockChangedAckPacket.create(instance.sequence());
        } else if (packet instanceof ClientboundSetBorderCenterPacket instance) {
            return SetBorderCenterPacket.create(new VirtualBorder.Center(instance.getNewCenterX(), instance.getNewCenterZ()));
        } else if (packet instanceof ClientboundAddExperienceOrbPacket instance) {
            Position position = new Position(instance.getX(), instance.getY(), instance.getZ());
            return AddExperienceOrbPacket.create(instance.getId(), position, instance.getValue());
        } else if (packet instanceof ClientboundMerchantOffersPacket instance) {
            List<MerchantOffersPacket.Offer> offers = new ArrayList<>();
            instance.getOffers().forEach(offer -> offers.add(wrap(offer)));
            return MerchantOffersPacket.create(instance.getContainerId(), offers, instance.getVillagerLevel(),
                    instance.getVillagerXp(), instance.showProgress(), instance.canRestock());
        } else if (packet instanceof ClientboundRemoveEntitiesPacket instance) {
            return RemoveEntitiesPacket.create(instance.getEntityIds().toArray(new int[]{}));
        } else if (packet instanceof ClientboundSetBorderWarningDistancePacket instance) {
            return SetBorderWarningDistancePacket.create(instance.getWarningBlocks());
        } else if (packet instanceof ClientboundSetSubtitleTextPacket instance) {
            return TitlePacket.SetSubtitleText.create(wrap(instance.getText()));
        } else if (packet instanceof ClientboundBlockEntityDataPacket instance) {
            return new BlockEntityDataPacket() {
                @Override
                public ClientboundBlockEntityDataPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundUpdateAttributesPacket instance) {
            return new UpdateAttributesPacket() {
                @Override
                public ClientboundUpdateAttributesPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundExplodePacket instance) {
            Position position = new Position(instance.getX(), instance.getY(), instance.getZ());
            List<BlockPosition> affectedBlock = new ArrayList<>();
            instance.getToBlow().forEach(pos -> affectedBlock.add(wrap(pos)));
            Vector knockback = new Vector(instance.getKnockbackX(), instance.getKnockbackY(), instance.getKnockbackZ());
            return ExplodePacket.create(position, instance.getPower(), affectedBlock, knockback);
        } else if (packet instanceof ClientboundPlayerCombatEnterPacket instance) {
            return PlayerCombatEnterPacket.create();
        } else if (packet instanceof ClientboundBlockEventPacket instance) {
            return BlockEventPacket.create(wrap(instance.getPos()), CraftMagicNumbers.getMaterial(instance.getBlock()), instance.getB0(), instance.getB1());
        } else if (packet instanceof ClientboundSetEntityLinkPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            SetEntityLinkPacket.create(buffer.readInt(), buffer.readInt());
        } else if (packet instanceof ClientboundCommandsPacket instance) {
            return new CommandsPacket() {
                @Override
                public ClientboundCommandsPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundLevelParticlesPacket instance) {
            return new LevelParticlesPacket() {
                @Override
                public ClientboundLevelParticlesPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPlayerCombatKillPacket instance) {
            return PlayerCombatKillPacket.create(instance.getPlayerId(), instance.getKillerId(), wrap(instance.getMessage()));
        } else if (packet instanceof ClientboundSetTitleTextPacket instance) {
            return TitlePacket.SetTitleText.create(wrap(instance.getText()));
        } else if (packet instanceof ClientboundSoundPacket instance) {
            return new SoundPacket() {
                @Override
                public ClientboundSoundPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundContainerSetSlotPacket instance) {
            return ContainerSetSlotPacket.create(instance.getContainerId(), instance.getSlot(), wrap(instance.getItem()));
        } else if (packet instanceof ClientboundRecipePacket instance) {
            return new RecipePacket() {
                @Override
                public ClientboundRecipePacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPlaceGhostRecipePacket instance) {
            return PlaceGhostRecipePacket.create(instance.getContainerId(), wrap(instance.getRecipe()));
        } else if (packet instanceof ClientboundBlockUpdatePacket instance) {
            return BlockUpdatePacket.create(wrap(instance.getPos()), Block.BLOCK_STATE_REGISTRY.getId(instance.getBlockState()));
        } else if (packet instanceof ClientboundSetDefaultSpawnPositionPacket instance) {
            return SetDefaultSpawnPositionPacket.create(wrap(instance.getPos()), instance.getAngle());
        } else if (packet instanceof ClientboundOpenScreenPacket instance) {
            assert instance.getType() != null : "The screen type cannot be null";
            return OpenScreenPacket.create(instance.getContainerId(), wrap(instance.getType()), wrap(instance.getTitle()));
        } else if (packet instanceof ClientboundSetEquipmentPacket instance) {
            HashMap<SlotType, TNLItem> items = new HashMap<>();
            instance.getSlots().forEach(pair -> items.put(wrap(pair.getFirst()), wrap(pair.getSecond())));
            return SetEquipmentPacket.create(instance.getEntity(), items);
        } else if (packet instanceof ClientboundSetTitlesAnimationPacket instance) {
            return TitlePacket.SetTitlesAnimation.create(instance.getFadeIn(), instance.getStay(), instance.getFadeOut());
        } else if (packet instanceof ClientboundMoveEntityPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            int entityId = buffer.readVarInt();
            if (instance.hasPosition() && instance.hasRotation()) {
                return MoveEntityPacket.PositionRotation.create(entityId, instance.getXa(), instance.getYa(), instance.getZa(), instance.getyRot(), instance.getxRot(), instance.isOnGround());
            } else if (instance.hasRotation()) {
                return MoveEntityPacket.Rotation.create(entityId, instance.getyRot(), instance.getxRot(), instance.isOnGround());
            } else if (instance.hasPosition()) {
                return MoveEntityPacket.Position.create(entityId, instance.getXa(), instance.getYa(), instance.getZa(), instance.isOnGround());
            }
        } else if (packet instanceof ClientboundAddPlayerPacket instance) {
            Position position = new Position(instance.getX(), instance.getY(), instance.getZ(), instance.getyRot(), instance.getxRot());
            return AddPlayerPacket.create(instance.getEntityId(), instance.getPlayerId(), position);
        } else if (packet instanceof ClientboundCustomChatCompletionsPacket instance) {
            return CustomChatCompletionsPacket.create(switch (instance.action()) {
                case ADD -> CustomChatCompletionsPacket.Action.ADD;
                case REMOVE -> CustomChatCompletionsPacket.Action.REMOVE;
                case SET -> CustomChatCompletionsPacket.Action.SET;
            }, instance.entries());
        } else if (packet instanceof ClientboundAwardStatsPacket instance) {
            return new AwardStatsPacket() {
                @Override
                public ClientboundAwardStatsPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPlayerPositionPacket instance) {
            return new PlayerPositionPacket() {
                @Override
                public ClientboundPlayerPositionPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPlayerInfoUpdatePacket instance) {
            List<PlayerInfoUpdatePacket.Action> actions = new ArrayList<>();
            List<PlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();
            instance.actions().forEach(action -> actions.add(wrap(action)));
            instance.entries().forEach(entry -> entries.add(wrap(entry)));
            return PlayerInfoUpdatePacket.create(actions, entries);
        } else if (packet instanceof ClientboundPlayerInfoRemovePacket instance) {
            return playerInfoRemovePacket(instance.profileIds());
        } else if (packet instanceof ClientboundSetObjectivePacket instance) {
            return new SetObjectivePacket() {
                @Override
                public ClientboundSetObjectivePacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundPlayerCombatEndPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            return PlayerCombatEndPacket.create(buffer.readVarInt(), buffer.readInt());
        } else if (packet instanceof ClientboundEntityEventPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            return EntityEventPacket.create(buffer.readInt(), buffer.readByte());
        } else if (packet instanceof ClientboundDeleteChatPacket instance) {
            return new DeleteChatPacket() {
                @Override
                public ClientboundDeleteChatPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundContainerSetDataPacket instance) {
            return ContainerSetDataPacket.create(instance.getContainerId(), instance.getId(), instance.getValue());
        } else if (packet instanceof ClientboundSetEntityMotionPacket instance) {
            Vector velocity = new Vector(instance.getXa(), instance.getYa(), instance.getZa());
            return SetEntityMotionPacket.create(instance.getId(), velocity);
        } else if (packet instanceof ClientboundSetBorderSizePacket instance) {
            return SetBorderSizePacket.create(instance.getSize());
        } else if (packet instanceof ClientboundPlayerChatPacket instance) {
            return PlayerChatPacket.create(instance.sender(), instance.index(), nullable(instance.signature()), wrap(instance.body()), nullable(instance.unsignedContent()), wrap(instance.filterMask()), wrap(instance.chatType()));
        } else if (packet instanceof ClientboundSetBorderWarningDelayPacket instance) {
            return SetBorderWarningDelayPacket.create(instance.getWarningDelay());
        } else if (packet instanceof ClientboundTabListPacket instance) {
            @SuppressWarnings("ConstantConditions")
            var header = instance.adventure$header != null ? instance.adventure$header : wrap(instance.getHeader());
            @SuppressWarnings("ConstantConditions")
            var footer = instance.adventure$footer != null ? instance.adventure$footer : wrap(instance.getHeader());
            return TabListPacket.create(header, footer);
        } else if (packet instanceof ClientboundChangeDifficultyPacket instance) {
            return ChangeDifficultyPacket.create(wrap(instance.getDifficulty()), instance.isLocked());
        } else if (packet instanceof ClientboundKeepAlivePacket instance) {
            return KeepAlivePacket.create(instance.getId());
        } else if (packet instanceof ClientboundClearTitlesPacket instance) {
            return TitlePacket.ClearTitles.create(instance.shouldResetTimes());
        } else if (packet instanceof ClientboundSetActionBarTextPacket instance) {
            return SetActionBarTextPacket.create(nullable(instance.getText()));
        } else if (packet instanceof ClientboundMapItemDataPacket instance) {
            return new MapItemDataPacket() {
                @Override
                public ClientboundMapItemDataPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundForgetLevelChunkPacket instance) {
            return ForgetLevelChunkPacket.create(instance.getX(), instance.getZ());
        } else if (packet instanceof ClientboundPlayerAbilitiesPacket instance) {
            return new PlayerAbilitiesPacket() {
                @Override
                public ClientboundPlayerAbilitiesPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundResourcePackPacket instance) {
            return ResourcePackPacket.create(instance.getUrl(), instance.getHash(), nullable(instance.getPrompt()), instance.isRequired());
        } else if (packet instanceof ClientboundCooldownPacket instance) {
            return CooldownPacket.create(wrap(instance.getItem()), instance.getDuration());
        } else if (packet instanceof ClientboundContainerClosePacket instance) {
            return ContainerClosePacket.create(instance.getContainerId());
        } else if (packet instanceof ClientboundTeleportEntityPacket instance) {
            Position position = new Position(instance.getX(), instance.getY(), instance.getZ(), instance.getyRot(), instance.getxRot());
            return TeleportEntityPacket.create(instance.getId(), position, instance.isOnGround());
        } else if (packet instanceof ClientboundRespawnPacket instance) {
            return new RespawnPacket() {
                @Override
                public ClientboundRespawnPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundBossEventPacket instance) {
            // write to buffer and read values UUID, Enum OperationType,
        } else if (packet instanceof ClientboundSystemChatPacket instance) {
            if (instance.adventure$content() != null) {
                return SystemChatPacket.create(instance.adventure$content(), instance.overlay());
            } else if (instance.content() != null) {
                return SystemChatPacket.create(net.kyori.adventure.text.Component.text(instance.content()), instance.overlay());
            } else throw new IllegalArgumentException("Must supply either adventure component or string json content");
        } else if (packet instanceof ClientboundAddEntityPacket instance) {
            Position position = new Position(instance.getX(), instance.getY(), instance.getZ(), instance.getYRot(), instance.getXRot());
            return AddEntityPacket.create(instance.getId(), instance.getUUID(), position, wrap(instance.getType()),
                    instance.getData(), new Vector(instance.getXa(), instance.getYa(), instance.getZa()), instance.getYHeadRot());
        } else if (packet instanceof ClientboundLevelEventPacket instance) {
            return LevelEventPacket.create(instance.getType(), wrap(instance.getPos()), instance.getData(), instance.isGlobalEvent());
        } else if (packet instanceof ClientboundUpdateMobEffectPacket instance) {
            return new UpdateMobEffectPacket() {
                @Override
                public ClientboundUpdateMobEffectPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundSetBorderLerpSizePacket instance) {
            return SetBorderLerpSizePacket.create(instance.getOldSize(), instance.getNewSize(), instance.getLerpTime());
        } else if (packet instanceof ClientboundUpdateAdvancementsPacket instance) {
            HashMap<NamespacedKey, Advancement.Builder> added = new HashMap<>();
            List<NamespacedKey> removed = new ArrayList<>();
            HashMap<NamespacedKey, Advancement.Progress> progress = new HashMap<>();
            instance.getAdded().forEach((resource, builder) -> added.put(wrap(resource), wrap(builder, resource)));
            instance.getRemoved().forEach(resource -> removed.add(wrap(resource)));
            instance.getProgress().forEach((resource, advancementProgress) -> progress.put(wrap(resource), wrap(advancementProgress)));
            return new UpdateAdvancementsPacket(instance.shouldReset(), added, removed, progress) {
                @Override
                public ClientboundUpdateAdvancementsPacket build() {
                    return instance;
                }
            };
            // return UpdateAdvancementsPacket.create(instance.shouldReset(), added, removed, progress);
        } else if (packet instanceof ClientboundRemoveMobEffectPacket instance) {
            return new RemoveMobEffectPacket() {
                @Override
                public ClientboundRemoveMobEffectPacket build() {
                    return instance;
                }
            };
        } else if (packet instanceof ClientboundSetHealthPacket instance) {
            return SetHealthPacket.create(instance.getHealth(), instance.getFood(), instance.getSaturation());
        } else if (packet instanceof ClientboundServerDataPacket instance) {
            return ServerDataPacket.create(nullable(instance.getMotd().orElse(null)), instance.getIconBase64().orElse(null), instance.enforcesSecureChat());
        } else if (packet instanceof ClientboundSetPassengersPacket instance) {
            return SetPassengersPacket.create(instance.getVehicle(), instance.getPassengers());
        } else if (packet instanceof ClientboundSetScorePacket instance) {
            return SetScorePacket.create(switch (instance.getMethod()) {
                case REMOVE -> SetScorePacket.Method.REMOVE;
                case CHANGE -> SetScorePacket.Method.UPDATE;
            }, instance.getObjectiveName(), instance.getOwner(), instance.getScore());
        } else if (packet instanceof ClientboundPlayerLookAtPacket instance) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            instance.write(buffer);
            EntityAnchorArgument.Anchor from = buffer.readEnum(EntityAnchorArgument.Anchor.class), to = null;
            double x = buffer.readDouble(), y = buffer.readDouble(), z = buffer.readDouble();
            int entityId = 0;
            if (buffer.readBoolean()) {
                entityId = buffer.readVarInt();
                to = buffer.readEnum(EntityAnchorArgument.Anchor.class);
            }
            return PlayerLookAtPacket.create(wrap(from), new Position(x, y, z), entityId, nullable(to));
        } else if (packet instanceof ClientboundLevelChunkWithLightPacket instance) {
            return original.apply(packet);
        }
        if (!unmapped.contains(packet.getClass().getName())) {
            unmapped.add(packet.getClass().getName());
            logger.warn("Unmapped outgoing (vanilla) packet: " + packet.getClass().getName());
        }
        return original.apply(packet);
    }

    private static final List<String> unmapped = new ArrayList<>();
}
