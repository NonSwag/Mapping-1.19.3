package net.nonswag.tnl.mapping.v1_19_R2.api.packets.incoming;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.logger.Logger;
import net.nonswag.tnl.listener.api.chat.ChatSession;
import net.nonswag.tnl.listener.api.chat.LastSeenMessages;
import net.nonswag.tnl.listener.api.chat.MessageSignature;
import net.nonswag.tnl.listener.api.gui.Interaction;
import net.nonswag.tnl.listener.api.item.TNLItem;
import net.nonswag.tnl.listener.api.location.BlockPosition;
import net.nonswag.tnl.listener.api.location.Direction;
import net.nonswag.tnl.listener.api.location.Position;
import net.nonswag.tnl.listener.api.packets.incoming.*;
import net.nonswag.tnl.listener.api.player.Hand;
import net.nonswag.tnl.listener.api.player.manager.ResourceManager;
import org.bukkit.Difficulty;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.block.structure.Mirror;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.nullable;
import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.wrap;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class IncomingPacketManager implements Incoming {

    @Override
    public AcceptTeleportationPacket acceptTeleportationPacket(int id) {
        return new AcceptTeleportationPacket(id) {
            @Override
            public ServerboundAcceptTeleportationPacket build() {
                return new ServerboundAcceptTeleportationPacket(getId());
            }
        };
    }

    @Override
    public BlockEntityTagQueryPacket blockEntityTagQueryPacket(int transactionId, BlockPosition position) {
        return new BlockEntityTagQueryPacket(transactionId, position) {
            @Override
            public ServerboundBlockEntityTagQuery build() {
                return new ServerboundBlockEntityTagQuery(getTransactionId(), wrap(getPosition()));
            }
        };
    }

    @Override
    public ChangeDifficultyPacket changeDifficultyPacket(Difficulty difficulty) {
        return new ChangeDifficultyPacket(difficulty) {
            @Override
            public ServerboundChangeDifficultyPacket build() {
                return new ServerboundChangeDifficultyPacket(wrap(getDifficulty()));
            }
        };
    }

    @Override
    public ChatAckPacket chatAckPacket(int offset) {
        return new ChatAckPacket(offset) {
            @Override
            public ServerboundChatAckPacket build() {
                return new ServerboundChatAckPacket(getOffset());
            }
        };
    }

    @Override
    public ChatCommandPacket chatCommandPacket(String command, Instant timeStamp, long salt, ChatCommandPacket.Entry[] argumentSignatures, LastSeenMessages.Update lastSeenMessages) {
        return new ChatCommandPacket(command, timeStamp, salt, argumentSignatures, lastSeenMessages) {
            @Override
            public ServerboundChatCommandPacket build() {
                return new ServerboundChatCommandPacket(getCommand(), getTimeStamp(), getSalt(), wrap(getArgumentSignatures()), wrap(getLastSeenMessages()));
            }
        };
    }

    @Override
    public ChatPacket chatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages) {
        return new ChatPacket(message, timeStamp, salt, signature, lastSeenMessages) {
            @Override
            public ServerboundChatPacket build() {
                return new ServerboundChatPacket(getMessage(), getTimeStamp(), getSalt(), nullable(getSignature()), wrap(getLastSeenMessages()));
            }
        };
    }

    @Override
    public ClientCommandPacket clientCommandPacket(ClientCommandPacket.Action action) {
        return new ClientCommandPacket(action) {
            @Override
            public ServerboundClientCommandPacket build() {
                return new ServerboundClientCommandPacket(switch (getAction()) {
                    case PERFORM_RESPAWN -> ServerboundClientCommandPacket.Action.PERFORM_RESPAWN;
                    case REQUEST_STATS -> ServerboundClientCommandPacket.Action.REQUEST_STATS;
                });
            }
        };
    }

    @Override
    public ClientInformationPacket clientInformationPacket(String language, int viewDistance, ClientInformationPacket.ChatVisibility chatVisibility, boolean chatColors, int modelCustomisation, Hand.Side mainHand, boolean textFiltering, boolean listingAllowed) {
        return new ClientInformationPacket(language, viewDistance, chatVisibility, chatColors, modelCustomisation, mainHand, textFiltering, listingAllowed) {
            @Override
            public ServerboundClientInformationPacket build() {
                return new ServerboundClientInformationPacket(getLanguage(), getViewDistance(), switch (getChatVisibility()) {
                    case FULL -> ChatVisiblity.FULL;
                    case SYSTEM -> ChatVisiblity.SYSTEM;
                    case HIDDEN -> ChatVisiblity.HIDDEN;
                }, isChatColors(), getModelCustomisation(), switch (getMainHand()) {
                    case LEFT -> HumanoidArm.LEFT;
                    case RIGHT -> HumanoidArm.RIGHT;
                }, isTextFiltering(), isListingAllowed());
            }
        };
    }

    @Override
    public CommandSuggestionPacket commandSuggestionPacket(int id, String command) {
        return new CommandSuggestionPacket(id, command) {
            @Override
            public ServerboundCommandSuggestionPacket build() {
                return new ServerboundCommandSuggestionPacket(getId(), getCommand());
            }
        };
    }

    @Override
    public CustomPayloadPacket customPayloadPacket(NamespacedKey channel, byte[] data) {
        return new CustomPayloadPacket(channel, data) {
            @Override
            public ServerboundCustomPayloadPacket build() {
                return new ServerboundCustomPayloadPacket(wrap(channel), new FriendlyByteBuf(Unpooled.buffer()).writeByteArray(data));
            }
        };
    }

    @Override
    public EditBookPacket editBookPacket(@Nullable String title, List<String> pages, int slot) {
        return new EditBookPacket(title, pages, slot) {
            @Override
            public ServerboundEditBookPacket build() {
                return new ServerboundEditBookPacket(getSlot(), getPages(), Optional.ofNullable(getTitle()));
            }
        };
    }

    @Override
    public EntityTagQueryPacket entityTagQueryPacket(int transactionId, int entityId) {
        return new EntityTagQueryPacket(transactionId, entityId) {
            @Override
            public ServerboundEntityTagQuery build() {
                return new ServerboundEntityTagQuery(getTransactionId(), getEntityId());
            }
        };
    }

    @Override
    public InteractPacket.Attack attack(int entityId, boolean sneaking) {
        return new InteractPacket.Attack(entityId, sneaking) {
            @Override
            public ServerboundInteractPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeEnum(ServerboundInteractPacket.ActionType.ATTACK);
                buffer.writeBoolean(isSneaking());
                return new ServerboundInteractPacket(buffer);
            }
        };
    }

    @Override
    public InteractPacket.Interact interactPacket(int entityId, boolean sneaking, Hand hand) {
        return new InteractPacket.Interact(entityId, sneaking, hand) {
            @Override
            public ServerboundInteractPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeEnum(ServerboundInteractPacket.ActionType.INTERACT);
                buffer.writeEnum(wrap(getHand()));
                buffer.writeBoolean(isSneaking());
                return new ServerboundInteractPacket(buffer);
            }
        };
    }

    @Override
    public InteractPacket.InteractAt interactAtPacket(int entityId, boolean sneaking, Hand hand, Vector location) {
        return new InteractPacket.InteractAt(entityId, sneaking, hand, location) {
            @Override
            public ServerboundInteractPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeEnum(ServerboundInteractPacket.ActionType.INTERACT_AT);
                buffer.writeFloat((float) getLocation().getX());
                buffer.writeFloat((float) getLocation().getY());
                buffer.writeFloat((float) getLocation().getZ());
                buffer.writeEnum(wrap(getHand()));
                buffer.writeBoolean(isSneaking());
                return new ServerboundInteractPacket(buffer);
            }
        };
    }

    @Override
    public JigsawGeneratePacket jigsawGeneratePacket(BlockPosition position, int levels, boolean keepJigsaws) {
        return new JigsawGeneratePacket(position, levels, keepJigsaws) {
            @Override
            public ServerboundJigsawGeneratePacket build() {
                return new ServerboundJigsawGeneratePacket(wrap(getPosition()), getLevels(), isKeepJigsaws());
            }
        };
    }

    @Override
    public KeepAlivePacket keepAlivePacket(long id) {
        return new KeepAlivePacket(id) {
            @Override
            public ServerboundKeepAlivePacket build() {
                return new ServerboundKeepAlivePacket(getId());
            }
        };
    }

    @Override
    public LockDifficultyPacket lockDifficultyPacket(boolean locked) {
        return new LockDifficultyPacket(locked) {
            @Override
            public ServerboundLockDifficultyPacket build() {
                return new ServerboundLockDifficultyPacket(isLocked());
            }
        };
    }

    @Override
    public MovePlayerPacket.Position movePlayerPacket(double x, double y, double z, boolean onGround) {
        return new MovePlayerPacket.Position(x, y, z, onGround) {
            @Override
            public ServerboundMovePlayerPacket.Pos build() {
                return new ServerboundMovePlayerPacket.Pos(getX(), getY(), getZ(), isOnGround());
            }
        };
    }

    @Override
    public MovePlayerPacket.PositionRotation movePlayerPacket(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new MovePlayerPacket.PositionRotation(x, y, z, yaw, pitch, onGround) {
            @Override
            public ServerboundMovePlayerPacket.PosRot build() {
                return new ServerboundMovePlayerPacket.PosRot(getX(), getY(), getZ(), getYaw(), getPitch(), isOnGround());
            }
        };
    }

    @Override
    public MovePlayerPacket.Rotation movePlayerPacket(float yaw, float pitch, boolean onGround) {
        return new MovePlayerPacket.Rotation(yaw, pitch, onGround) {
            @Override
            public ServerboundMovePlayerPacket.Rot build() {
                return new ServerboundMovePlayerPacket.Rot(getYaw(), getPitch(), isOnGround());
            }
        };
    }

    @Override
    public MovePlayerPacket.Status movePlayerPacket(boolean onGround) {
        return new MovePlayerPacket.Status(onGround) {
            @Override
            public ServerboundMovePlayerPacket.StatusOnly build() {
                return new ServerboundMovePlayerPacket.StatusOnly(isOnGround());
            }
        };
    }

    @Override
    public MoveVehiclePacket moveVehiclePacket(Position position) {
        return new MoveVehiclePacket(position) {
            @Override
            public ServerboundMoveVehiclePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeDouble(getPosition().getX());
                buffer.writeDouble(getPosition().getY());
                buffer.writeDouble(getPosition().getZ());
                buffer.writeFloat(getPosition().getYaw());
                buffer.writeFloat(getPosition().getPitch());
                return new ServerboundMoveVehiclePacket(buffer);
            }
        };
    }

    @Override
    public PaddleBoatPacket paddleBoatPacket(boolean left, boolean right) {
        return new PaddleBoatPacket(left, right) {
            @Override
            public ServerboundPaddleBoatPacket build() {
                return new ServerboundPaddleBoatPacket(isLeft(), isRight());
            }
        };
    }

    @Override
    public PickItemPacket pickItemPacket(int slot) {
        return new PickItemPacket(slot) {
            @Override
            public ServerboundPickItemPacket build() {
                return new ServerboundPickItemPacket(getSlot());
            }
        };
    }

    @Override
    public PlaceRecipePacket placeRecipePacket(int containerId, NamespacedKey recipe, boolean shift) {
        return new PlaceRecipePacket(containerId, recipe, shift) {
            @Override
            public ServerboundPlaceRecipePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeByte(getContainerId());
                buffer.writeResourceLocation(wrap(getRecipe()));
                buffer.writeBoolean(isShift());
                return new ServerboundPlaceRecipePacket(buffer);
            }
        };
    }

    @Override
    public PlayerAbilitiesPacket playerAbilitiesPacket(boolean flying) {
        return new PlayerAbilitiesPacket(flying) {
            @Override
            public ServerboundPlayerAbilitiesPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeByte(isFlying() ? 2 : 0);
                return new ServerboundPlayerAbilitiesPacket(buffer);
            }
        };
    }

    @Override
    public PlayerActionPacket playerActionPacket(PlayerActionPacket.Action action, BlockPosition position, Direction direction, int sequence) {
        return new PlayerActionPacket(action, position, direction, sequence) {
            @Override
            public ServerboundPlayerActionPacket build() {
                return new ServerboundPlayerActionPacket(switch (getAction()) {
                    case START_DESTROY_BLOCK -> ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK;
                    case ABORT_DESTROY_BLOCK -> ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK;
                    case STOP_DESTROY_BLOCK -> ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK;
                    case DROP_ALL_ITEMS -> ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS;
                    case DROP_ITEM -> ServerboundPlayerActionPacket.Action.DROP_ITEM;
                    case RELEASE_USE_ITEM -> ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM;
                    case SWAP_ITEM_WITH_OFFHAND -> ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND;
                }, wrap(getPosition()), wrap(getDirection()), getSequence());
            }
        };
    }

    @Override
    public PlayerCommandPacket playerCommandPacket(int entityId, PlayerCommandPacket.Action action, int data) {
        return new PlayerCommandPacket(entityId, action, data) {
            @Override
            public ServerboundPlayerCommandPacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeVarInt(getEntityId());
                buffer.writeEnum(switch (getAction()) {
                    case PRESS_SHIFT_KEY -> ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY;
                    case RELEASE_SHIFT_KEY -> ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
                    case STOP_SLEEPING -> ServerboundPlayerCommandPacket.Action.STOP_SLEEPING;
                    case START_SPRINTING -> ServerboundPlayerCommandPacket.Action.START_SPRINTING;
                    case STOP_SPRINTING -> ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
                    case START_RIDING_JUMP -> ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP;
                    case STOP_RIDING_JUMP -> ServerboundPlayerCommandPacket.Action.STOP_RIDING_JUMP;
                    case OPEN_INVENTORY -> ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY;
                    case START_FALL_FLYING -> ServerboundPlayerCommandPacket.Action.START_FALL_FLYING;
                });
                buffer.writeVarInt(getData());
                return new ServerboundPlayerCommandPacket(buffer);
            }
        };
    }

    @Override
    public PlayerInputPacket playerInputPacket(float sideways, float forward, boolean jumping, boolean sneaking) {
        return new PlayerInputPacket(sideways, forward, jumping, sneaking) {
            @Override
            public ServerboundPlayerInputPacket build() {
                return new ServerboundPlayerInputPacket(getSideways(), getForward(), isJumping(), isSneaking());
            }
        };
    }

    @Override
    public PongPacket pongPacket(int id) {
        return new PongPacket(id) {
            @Override
            public ServerboundPongPacket build() {
                return new ServerboundPongPacket(getId());
            }
        };
    }

    @Override
    public RecipeBookChangeSettingsPacket recipeBookChangeSettingsPacket(RecipeBookChangeSettingsPacket.RecipeBookType category, boolean guiOpen, boolean filteringCraftable) {
        return new RecipeBookChangeSettingsPacket(category, guiOpen, filteringCraftable) {
            @Override
            public ServerboundRecipeBookChangeSettingsPacket build() {
                return new ServerboundRecipeBookChangeSettingsPacket(switch (getCategory()) {
                    case CRAFTING -> net.minecraft.world.inventory.RecipeBookType.CRAFTING;
                    case FURNACE -> net.minecraft.world.inventory.RecipeBookType.FURNACE;
                    case BLAST_FURNACE -> net.minecraft.world.inventory.RecipeBookType.BLAST_FURNACE;
                    case SMOKER -> net.minecraft.world.inventory.RecipeBookType.SMOKER;
                }, isGuiOpen(), isFilteringCraftable());
            }
        };
    }

    @Override
    public RecipeBookSeenRecipePacket recipeBookSeenRecipePacket(NamespacedKey recipe) {
        return new RecipeBookSeenRecipePacket(recipe) {
            @Override
            public ServerboundRecipeBookSeenRecipePacket build() {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeResourceLocation(wrap(getRecipe()));
                return new ServerboundRecipeBookSeenRecipePacket(buffer);
            }
        };
    }

    @Override
    public RenameItemPacket renameItemPacket(String name) {
        return new RenameItemPacket(name) {
            @Override
            public ServerboundRenameItemPacket build() {
                return new ServerboundRenameItemPacket(getName());
            }
        };
    }

    @Override
    public ResourcePackPacket resourcePackPacket(ResourceManager.Action action) {
        return new ResourcePackPacket(action) {
            @Override
            public ServerboundResourcePackPacket build() {
                return new ServerboundResourcePackPacket(switch (getAction()) {
                    case SUCCESSFULLY_LOADED -> ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED;
                    case DECLINED -> ServerboundResourcePackPacket.Action.DECLINED;
                    case FAILED_DOWNLOAD -> ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD;
                    case ACCEPTED -> ServerboundResourcePackPacket.Action.ACCEPTED;
                });
            }
        };
    }

    @Override
    public SeenAdvancementsPacket seenAdvancementsPacket(SeenAdvancementsPacket.Action action, @Nullable NamespacedKey tab) {
        return new SeenAdvancementsPacket(action, tab) {
            @Override
            public ServerboundSeenAdvancementsPacket build() {
                return new ServerboundSeenAdvancementsPacket(switch (getAction()) {
                    case OPENED_TAB -> ServerboundSeenAdvancementsPacket.Action.OPENED_TAB;
                    case CLOSED_SCREEN -> ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN;
                }, nullable(getTab()));
            }
        };
    }

    @Override
    public SelectTradePacket selectTradePacket(int trade) {
        return new SelectTradePacket(trade) {
            @Override
            public ServerboundSelectTradePacket build() {
                return new ServerboundSelectTradePacket(getTrade());
            }
        };
    }

    @Override
    public SetBeaconPacket setBeaconPacket(@Nullable SetBeaconPacket.Effect primary, @Nullable SetBeaconPacket.Effect secondary) {
        return new SetBeaconPacket(primary, secondary) {
            @Override
            public ServerboundSetBeaconPacket build() {
                return new ServerboundSetBeaconPacket(Optional.ofNullable(nullable(getPrimary())), Optional.ofNullable(nullable(getSecondary())));
            }
        };
    }

    @Override
    public SetCarriedItemPacket setCarriedItemPacket(int slot) {
        return new SetCarriedItemPacket(slot) {
            @Override
            public ServerboundSetCarriedItemPacket build() {
                return new ServerboundSetCarriedItemPacket(getSlot());
            }
        };
    }

    @Override
    public SetCommandBlockPacket setCommandBlockPacket(BlockPosition position, String command, SetCommandBlockPacket.Mode mode, boolean trackOutput, boolean conditional, boolean alwaysActive) {
        return new SetCommandBlockPacket(position, command, mode, trackOutput, conditional, alwaysActive) {
            @Override
            public ServerboundSetCommandBlockPacket build() {
                return new ServerboundSetCommandBlockPacket(wrap(getPosition()), getCommand(), switch (mode) {
                    case SEQUENCE -> CommandBlockEntity.Mode.SEQUENCE;
                    case AUTO -> CommandBlockEntity.Mode.AUTO;
                    case REDSTONE -> CommandBlockEntity.Mode.REDSTONE;
                }, isTrackOutput(), isConditional(), isAlwaysActive());
            }
        };
    }

    @Override
    public SetCommandMinecartPacket setCommandMinecartPacket(int entityId, String command, boolean trackOutput) {
        return new SetCommandMinecartPacket(entityId, command, trackOutput) {
            @Override
            public ServerboundSetCommandMinecartPacket build() {
                return new ServerboundSetCommandMinecartPacket(getEntityId(), getCommand(), isTrackOutput());
            }
        };
    }

    @Override
    public SetCreativeModeSlotPacket setCreativeModeSlotPacket(int slot, TNLItem item) {
        return new SetCreativeModeSlotPacket(slot, item) {
            @Override
            public ServerboundSetCreativeModeSlotPacket build() {
                return new ServerboundSetCreativeModeSlotPacket(getSlot(), wrap(getItem()));
            }
        };
    }

    @Override
    public SetJigsawBlockPacket setJigsawBlockPacket(BlockPosition position, NamespacedKey name, NamespacedKey target, NamespacedKey pool, String finalState, SetJigsawBlockPacket.JointType joint) {
        return new SetJigsawBlockPacket(position, name, target, pool, finalState, joint) {
            @Override
            public ServerboundSetJigsawBlockPacket build() {
                return new ServerboundSetJigsawBlockPacket(wrap(getPosition()), wrap(getName()), wrap(getTarget()),
                        wrap(getPool()), getFinalState(), switch (getJoint()) {
                    case ROLLABLE -> JigsawBlockEntity.JointType.ROLLABLE;
                    case ALIGNED -> JigsawBlockEntity.JointType.ALIGNED;
                });
            }
        };
    }

    @Override
    public SetStructureBlockPacket setStructureBlockPacket(BlockPosition position, SetStructureBlockPacket.Type type, SetStructureBlockPacket.Mode mode, String name, BlockPosition offset, Vector size, Mirror mirror, Rotation rotation, String metadata, boolean ignoreEntities, boolean showAir, boolean showBoundingBox, float integrity, long seed) {
        return new SetStructureBlockPacket(position, type, mode, name, offset, size, mirror, rotation, metadata, ignoreEntities, showAir, showBoundingBox, integrity, seed) {
            @Override
            public ServerboundSetStructureBlockPacket build() {
                return new ServerboundSetStructureBlockPacket(wrap(getPosition()), switch (getType()) {
                    case UPDATE_DATA -> StructureBlockEntity.UpdateType.UPDATE_DATA;
                    case SAVE_AREA -> StructureBlockEntity.UpdateType.SAVE_AREA;
                    case LOAD_AREA -> StructureBlockEntity.UpdateType.LOAD_AREA;
                    case SCAN_AREA -> StructureBlockEntity.UpdateType.SCAN_AREA;
                }, switch (getMode()) {
                    case SAVE -> StructureMode.SAVE;
                    case LOAD -> StructureMode.LOAD;
                    case CORNER -> StructureMode.CORNER;
                    case DATA -> StructureMode.DATA;
                }, getName(), wrap(getOffset()), wrap(getSize()), switch (getMirror()) {
                    case NONE -> net.minecraft.world.level.block.Mirror.NONE;
                    case FRONT_BACK -> net.minecraft.world.level.block.Mirror.FRONT_BACK;
                    case LEFT_RIGHT -> net.minecraft.world.level.block.Mirror.LEFT_RIGHT;
                }, switch (getRotation()) {
                    case NONE -> net.minecraft.world.level.block.Rotation.NONE;
                    case CLOCKWISE -> net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
                    case FLIPPED -> net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
                    case COUNTER_CLOCKWISE -> net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
                    default -> throw new IllegalStateException("Unexpected value: " + getRotation());
                }, getMetadata(), isIgnoreEntities(), isShowAir(), isShowBoundingBox(), getIntegrity(), getSeed());
            }
        };
    }

    @Override
    public SignUpdatePacket signUpdatePacket(BlockPosition position, String[] lines) {
        return new SignUpdatePacket(position, lines) {
            @Override
            public ServerboundSignUpdatePacket build() {
                return new ServerboundSignUpdatePacket(wrap(getPosition()), getLines()[0], getLines()[1], getLines()[2], getLines()[3]);
            }
        };
    }

    @Override
    public ChatSessionUpdatePacket chatSessionUpdatePacket(ChatSession session) {
        return new ChatSessionUpdatePacket(session) {
            @Override
            public ServerboundChatSessionUpdatePacket build() {
                return new ServerboundChatSessionUpdatePacket(wrap(getSession()));
            }
        };
    }

    @Override
    public SwingPacket swingPacket(Hand hand) {
        return new SwingPacket(hand) {
            @Override
            public ServerboundSwingPacket build() {
                return new ServerboundSwingPacket(wrap(getHand()));
            }
        };
    }

    @Override
    public TeleportToEntityPacket teleportToEntityPacket(UUID target) {
        return new TeleportToEntityPacket(target) {
            @Override
            public ServerboundTeleportToEntityPacket build() {
                return new ServerboundTeleportToEntityPacket(getTarget());
            }
        };
    }

    @Override
    public UseItemOnPacket useItemOnPacket(Hand hand, UseItemOnPacket.BlockTargetResult target, int sequence, long timestamp) {
        return new UseItemOnPacket(hand, target, sequence, timestamp) {
            @Override
            public ServerboundUseItemOnPacket build() {
                ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(wrap(getHand()), wrap(getTarget()), getSequence());
                packet.timestamp = getTimestamp();
                return packet;
            }
        };
    }

    @Override
    public UseItemPacket useItemPacket(Hand hand, int sequence) {
        return new UseItemPacket(hand, sequence) {
            @Override
            public ServerboundUseItemPacket build() {
                return new ServerboundUseItemPacket(wrap(getHand()), getSequence());
            }
        };
    }

    @Override
    public ContainerButtonClickPacket containerButtonClickPacket(int containerId, int buttonId) {
        return new ContainerButtonClickPacket(containerId, buttonId) {
            @Override
            public ServerboundContainerButtonClickPacket build() {
                return new ServerboundContainerButtonClickPacket(getContainerId(), getButtonId());
            }
        };
    }

    @Override
    public ContainerClickPacket containerClickPacket(int containerId, int stateId, int slot, int buttonId, ContainerClickPacket.ClickType clickType, TNLItem item, HashMap<Integer, TNLItem> changedSlots) {
        return new ContainerClickPacket(containerId, stateId, slot, buttonId, clickType, item, changedSlots) {
            @Override
            public Interaction.Type getInteractionType() {
                return switch (clickType) {
                    case PICKUP -> buttonId == 0 ? Interaction.Type.LEFT : Interaction.Type.RIGHT;
                    case QUICK_MOVE, QUICK_CRAFT -> buttonId == 0 ? Interaction.Type.SHIFT_LEFT : Interaction.Type.SHIFT_RIGHT;
                    case SWAP -> switch (buttonId) {
                        case 0 -> Interaction.Type.NUMBER_KEY_1;
                        case 1 -> Interaction.Type.NUMBER_KEY_2;
                        case 2 -> Interaction.Type.NUMBER_KEY_3;
                        case 3 -> Interaction.Type.NUMBER_KEY_4;
                        case 4 -> Interaction.Type.NUMBER_KEY_5;
                        case 5 -> Interaction.Type.NUMBER_KEY_6;
                        case 6 -> Interaction.Type.NUMBER_KEY_7;
                        case 7 -> Interaction.Type.NUMBER_KEY_8;
                        case 8 -> Interaction.Type.NUMBER_KEY_9;
                        case 40 -> Interaction.Type.OFF_HAND;
                        default -> Interaction.Type.GENERAL;
                    };
                    case CLONE -> Interaction.Type.MIDDLE;
                    case THROW -> buttonId == 0 ? Interaction.Type.DROP : Interaction.Type.DROP_ALL;
                    case PICKUP_ALL -> Interaction.Type.DOUBLE_CLICK;
                };
            }

            @Override
            public ServerboundContainerClickPacket build() {
                return new ServerboundContainerClickPacket(getContainerId(), getStateId(), getSlot(), getButtonId(), switch (getClickType()) {
                    case PICKUP -> net.minecraft.world.inventory.ClickType.PICKUP;
                    case QUICK_MOVE -> net.minecraft.world.inventory.ClickType.QUICK_MOVE;
                    case SWAP -> net.minecraft.world.inventory.ClickType.SWAP;
                    case CLONE -> net.minecraft.world.inventory.ClickType.CLONE;
                    case THROW -> net.minecraft.world.inventory.ClickType.THROW;
                    case QUICK_CRAFT -> net.minecraft.world.inventory.ClickType.QUICK_CRAFT;
                    case PICKUP_ALL -> net.minecraft.world.inventory.ClickType.PICKUP_ALL;
                }, wrap(getItem()), wrap(getChangedSlots()));
            }
        };
    }

    @Override
    public ContainerClosePacket containerClosePacket(int containerId) {
        return new ContainerClosePacket(containerId) {
            @Override
            public ServerboundContainerClosePacket build() {
                return new ServerboundContainerClosePacket(getContainerId());
            }
        };
    }

    private final HashMap<Class<? extends Packet<ServerGamePacketListener>>, Class<? extends PacketBuilder>> PACKET_MAP = new HashMap<>() {{
        put(ServerboundAcceptTeleportationPacket.class, AcceptTeleportationPacket.class);
        put(ServerboundBlockEntityTagQuery.class, BlockEntityTagQueryPacket.class);
        put(ServerboundChangeDifficultyPacket.class, ChangeDifficultyPacket.class);
        put(ServerboundChatAckPacket.class, ChatAckPacket.class);
        put(ServerboundChatCommandPacket.class, ChatCommandPacket.class);
        put(ServerboundChatPacket.class, ChatPacket.class);
        put(ServerboundChatSessionUpdatePacket.class, ChatSessionUpdatePacket.class);
        put(ServerboundClientCommandPacket.class, ClientCommandPacket.class);
        put(ServerboundClientInformationPacket.class, ClientInformationPacket.class);
        put(ServerboundCommandSuggestionPacket.class, CommandSuggestionPacket.class);
        put(ServerboundContainerButtonClickPacket.class, ContainerButtonClickPacket.class);
        put(ServerboundContainerClickPacket.class, ContainerClickPacket.class);
        put(ServerboundContainerClosePacket.class, ContainerClosePacket.class);
        put(ServerboundCustomPayloadPacket.class, CustomPayloadPacket.class);
        put(ServerboundEditBookPacket.class, EditBookPacket.class);
        put(ServerboundEntityTagQuery.class, EntityTagQueryPacket.class);
        put(ServerboundInteractPacket.class, InteractPacket.class);
        put(ServerboundJigsawGeneratePacket.class, JigsawGeneratePacket.class);
        put(ServerboundKeepAlivePacket.class, KeepAlivePacket.class);
        put(ServerboundLockDifficultyPacket.class, LockDifficultyPacket.class);
        put(ServerboundMovePlayerPacket.class, MovePlayerPacket.class);
        put(ServerboundMovePlayerPacket.Pos.class, MovePlayerPacket.Position.class);
        put(ServerboundMovePlayerPacket.Rot.class, MovePlayerPacket.Rotation.class);
        put(ServerboundMovePlayerPacket.PosRot.class, MovePlayerPacket.PositionRotation.class);
        put(ServerboundMovePlayerPacket.StatusOnly.class, MovePlayerPacket.Status.class);
        put(ServerboundMoveVehiclePacket.class, MoveVehiclePacket.class);
        put(ServerboundPaddleBoatPacket.class, PaddleBoatPacket.class);
        put(ServerboundPickItemPacket.class, PickItemPacket.class);
        put(ServerboundPlaceRecipePacket.class, PlaceRecipePacket.class);
        put(ServerboundPlayerAbilitiesPacket.class, PlayerAbilitiesPacket.class);
        put(ServerboundPlayerActionPacket.class, PlayerActionPacket.class);
        put(ServerboundPlayerCommandPacket.class, PlayerCommandPacket.class);
        put(ServerboundPlayerInputPacket.class, PlayerInputPacket.class);
        put(ServerboundPongPacket.class, PongPacket.class);
        put(ServerboundRecipeBookChangeSettingsPacket.class, RecipeBookChangeSettingsPacket.class);
        put(ServerboundRecipeBookSeenRecipePacket.class, RecipeBookSeenRecipePacket.class);
        put(ServerboundRenameItemPacket.class, RenameItemPacket.class);
        put(ServerboundResourcePackPacket.class, ResourcePackPacket.class);
        put(ServerboundSeenAdvancementsPacket.class, SeenAdvancementsPacket.class);
        put(ServerboundSelectTradePacket.class, SelectTradePacket.class);
        put(ServerboundSetBeaconPacket.class, SetBeaconPacket.class);
        put(ServerboundSetCarriedItemPacket.class, SetCarriedItemPacket.class);
        put(ServerboundSetCommandBlockPacket.class, SetCommandBlockPacket.class);
        put(ServerboundSetCommandMinecartPacket.class, SetCommandMinecartPacket.class);
        put(ServerboundSetCreativeModeSlotPacket.class, SetCreativeModeSlotPacket.class);
        put(ServerboundSetJigsawBlockPacket.class, SetJigsawBlockPacket.class);
        put(ServerboundSetStructureBlockPacket.class, SetStructureBlockPacket.class);
        put(ServerboundSignUpdatePacket.class, SignUpdatePacket.class);
        put(ServerboundSwingPacket.class, SwingPacket.class);
        put(ServerboundTeleportToEntityPacket.class, TeleportToEntityPacket.class);
        put(ServerboundUseItemOnPacket.class, UseItemOnPacket.class);
        put(ServerboundUseItemPacket.class, UseItemPacket.class);
    }};

    @Override
    public <P> Class<? extends IncomingPacket> map(Class<P> clazz) {
        Class<? extends PacketBuilder> aClass = PACKET_MAP.get(clazz);
        if (aClass != null) return aClass;
        throw new IllegalStateException("Unmapped incoming packet: " + clazz.getName());
    }

    @Override
    public <P> PacketBuilder map(P packet) {
        Function<P, PacketBuilder> original = p -> new PacketBuilder() {
            @Override
            public P build() {
                return p;
            }
        };
        if (packet instanceof ServerboundContainerClosePacket instance) {
            return ContainerClosePacket.create(instance.getContainerId());
        } else if (packet instanceof ServerboundResourcePackPacket instance) {
            return switch (instance.getAction()) {
                case ACCEPTED -> ResourcePackPacket.create(ResourceManager.Action.ACCEPTED);
                case DECLINED -> ResourcePackPacket.create(ResourceManager.Action.DECLINED);
                case FAILED_DOWNLOAD -> ResourcePackPacket.create(ResourceManager.Action.FAILED_DOWNLOAD);
                case SUCCESSFULLY_LOADED -> ResourcePackPacket.create(ResourceManager.Action.SUCCESSFULLY_LOADED);
            };
        } else if (packet instanceof ServerboundUseItemPacket instance) {
            return UseItemPacket.create(wrap(instance.getHand()), instance.getSequence());
        } else if (packet instanceof ServerboundTeleportToEntityPacket instance) {
            FriendlyByteBuf buffer;
            instance.write(buffer = new FriendlyByteBuf(Unpooled.buffer()));
            return TeleportToEntityPacket.create(buffer.readUUID());
        } else if (packet instanceof ServerboundSwingPacket instance) {
            return SwingPacket.create(wrap(instance.getHand()));
        } else if (packet instanceof ServerboundSetStructureBlockPacket instance) {
            return SetStructureBlockPacket.create(wrap(instance.getPos()), switch (instance.getUpdateType()) {
                case LOAD_AREA -> SetStructureBlockPacket.Type.LOAD_AREA;
                case SAVE_AREA -> SetStructureBlockPacket.Type.SAVE_AREA;
                case UPDATE_DATA -> SetStructureBlockPacket.Type.UPDATE_DATA;
                case SCAN_AREA -> SetStructureBlockPacket.Type.SCAN_AREA;
            }, switch (instance.getMode()) {
                case DATA -> SetStructureBlockPacket.Mode.DATA;
                case LOAD -> SetStructureBlockPacket.Mode.LOAD;
                case SAVE -> SetStructureBlockPacket.Mode.SAVE;
                case CORNER -> SetStructureBlockPacket.Mode.CORNER;
            }, instance.getName(), wrap(instance.getOffset()), wrap(instance.getSize()), switch (instance.getMirror()) {
                case NONE -> Mirror.NONE;
                case FRONT_BACK -> Mirror.FRONT_BACK;
                case LEFT_RIGHT -> Mirror.LEFT_RIGHT;
            }, switch (instance.getRotation()) {
                case NONE -> Rotation.NONE;
                case CLOCKWISE_90 -> Rotation.CLOCKWISE;
                case CLOCKWISE_180 -> Rotation.FLIPPED;
                case COUNTERCLOCKWISE_90 -> Rotation.COUNTER_CLOCKWISE;
            }, instance.getData(), instance.isIgnoreEntities(), instance.isShowAir(), instance.isShowBoundingBox(), instance.getIntegrity(), instance.getSeed());
        } else if (packet instanceof ServerboundSetJigsawBlockPacket instance) {
            return SetJigsawBlockPacket.create(wrap(instance.getPos()), wrap(instance.getName()), wrap(instance.getTarget()),
                    wrap(instance.getPool()), instance.getFinalState(), switch (instance.getJoint()) {
                        case ROLLABLE -> SetJigsawBlockPacket.JointType.ROLLABLE;
                        case ALIGNED -> SetJigsawBlockPacket.JointType.ALIGNED;
                    });
        } else if (packet instanceof ServerboundSetCreativeModeSlotPacket instance) {
            return SetCreativeModeSlotPacket.create(instance.getSlotNum(), wrap(instance.getItem()));
        } else if (packet instanceof ServerboundSetCommandMinecartPacket instance) {
            FriendlyByteBuf buffer;
            instance.write(buffer = new FriendlyByteBuf(Unpooled.buffer()));
            return SetCommandMinecartPacket.create(buffer.readVarInt(), buffer.readUtf(), buffer.readBoolean());
        } else if (packet instanceof ServerboundSetCommandBlockPacket instance) {
            return SetCommandBlockPacket.create(wrap(instance.getPos()), instance.getCommand(), switch (instance.getMode()) {
                case AUTO -> SetCommandBlockPacket.Mode.AUTO;
                case REDSTONE -> SetCommandBlockPacket.Mode.REDSTONE;
                case SEQUENCE -> SetCommandBlockPacket.Mode.SEQUENCE;
            }, instance.isTrackOutput(), instance.isConditional(), instance.isAutomatic());
        } else if (packet instanceof ServerboundSetCarriedItemPacket instance) {
            return SetCarriedItemPacket.create(instance.getSlot());
        } else if (packet instanceof ServerboundSetBeaconPacket instance) {
            return SetBeaconPacket.create(wrap(instance.getPrimary()), wrap(instance.getSecondary()));
        } else if (packet instanceof ServerboundSelectTradePacket instance) {
            return SelectTradePacket.create(instance.getItem());
        } else if (packet instanceof ServerboundSeenAdvancementsPacket instance) {
            return SeenAdvancementsPacket.create(switch (instance.getAction()) {
                case OPENED_TAB -> SeenAdvancementsPacket.Action.OPENED_TAB;
                case CLOSED_SCREEN -> SeenAdvancementsPacket.Action.CLOSED_SCREEN;
            }, nullable(instance.getTab()));
        } else if (packet instanceof ServerboundRecipeBookSeenRecipePacket instance) {
            return RecipeBookSeenRecipePacket.create(wrap(instance.getRecipe()));
        } else if (packet instanceof ServerboundRecipeBookChangeSettingsPacket instance) {
            return RecipeBookChangeSettingsPacket.create(switch (instance.getBookType()) {
                case CRAFTING -> RecipeBookChangeSettingsPacket.RecipeBookType.CRAFTING;
                case FURNACE -> RecipeBookChangeSettingsPacket.RecipeBookType.FURNACE;
                case BLAST_FURNACE -> RecipeBookChangeSettingsPacket.RecipeBookType.BLAST_FURNACE;
                case SMOKER -> RecipeBookChangeSettingsPacket.RecipeBookType.SMOKER;
            }, instance.isOpen(), instance.isFiltering());
        } else if (packet instanceof ServerboundPongPacket instance) {
            return PongPacket.create(instance.getId());
        } else if (packet instanceof ServerboundPlayerCommandPacket instance) {
            return PlayerCommandPacket.create(instance.getId(), switch (instance.getAction()) {
                case PRESS_SHIFT_KEY -> PlayerCommandPacket.Action.PRESS_SHIFT_KEY;
                case RELEASE_SHIFT_KEY -> PlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
                case STOP_SLEEPING -> PlayerCommandPacket.Action.STOP_SLEEPING;
                case START_SPRINTING -> PlayerCommandPacket.Action.START_SPRINTING;
                case STOP_SPRINTING -> PlayerCommandPacket.Action.STOP_SPRINTING;
                case START_RIDING_JUMP -> PlayerCommandPacket.Action.START_RIDING_JUMP;
                case STOP_RIDING_JUMP -> PlayerCommandPacket.Action.STOP_RIDING_JUMP;
                case OPEN_INVENTORY -> PlayerCommandPacket.Action.OPEN_INVENTORY;
                case START_FALL_FLYING -> PlayerCommandPacket.Action.START_FALL_FLYING;
            }, instance.getData());
        } else if (packet instanceof ServerboundPlayerAbilitiesPacket instance) {
            return PlayerAbilitiesPacket.create(instance.isFlying());
        } else if (packet instanceof ServerboundPlaceRecipePacket instance) {
            return PlaceRecipePacket.create(instance.getContainerId(), wrap(instance.getRecipe()), instance.isShiftDown());
        } else if (packet instanceof ServerboundPaddleBoatPacket instance) {
            return PaddleBoatPacket.create(instance.getLeft(), instance.getRight());
        } else if (packet instanceof ServerboundMoveVehiclePacket instance) {
            return MoveVehiclePacket.create(new Position(instance.getX(), instance.getY(), instance.getZ(), instance.getYRot(), instance.getXRot()));
        } else if (packet instanceof ServerboundMovePlayerPacket instance) {
            if (instance.hasPosition() && instance.hasRotation()) {
                return MovePlayerPacket.PositionRotation.create(instance.x, instance.y, instance.z, instance.yRot, instance.xRot, instance.isOnGround());
            } else if (instance.hasRotation()) {
                return MovePlayerPacket.Rotation.create(instance.yRot, instance.xRot, instance.isOnGround());
            } else if (instance.hasPosition()) {
                return MovePlayerPacket.Position.create(instance.x, instance.y, instance.z, instance.isOnGround());
            } else return MovePlayerPacket.Status.create(instance.isOnGround());
        } else if (packet instanceof ServerboundLockDifficultyPacket instance) {
            return LockDifficultyPacket.create(instance.isLocked());
        } else if (packet instanceof ServerboundKeepAlivePacket instance) {
            return KeepAlivePacket.create(instance.getId());
        } else if (packet instanceof ServerboundChatSessionUpdatePacket instance) {
            return ChatSessionUpdatePacket.create(wrap(instance.chatSession()));
        } else if (packet instanceof ServerboundJigsawGeneratePacket instance) {
            return JigsawGeneratePacket.create(wrap(instance.getPos()), instance.levels(), instance.keepJigsaws());
        } else if (packet instanceof ServerboundEntityTagQuery instance) {
            return EntityTagQueryPacket.create(instance.getTransactionId(), instance.getEntityId());
        } else if (packet instanceof ServerboundEditBookPacket instance) {
            return EditBookPacket.create(instance.getTitle().orElse(null), instance.getPages(), instance.getSlot());
        } else if (packet instanceof ServerboundContainerButtonClickPacket instance) {
            return ContainerButtonClickPacket.create(instance.getContainerId(), instance.getButtonId());
        } else if (packet instanceof ServerboundClientInformationPacket instance) {
            return ClientInformationPacket.create(instance.language(), instance.viewDistance(), switch (instance.chatVisibility()) {
                case FULL -> ClientInformationPacket.ChatVisibility.FULL;
                case SYSTEM -> ClientInformationPacket.ChatVisibility.SYSTEM;
                case HIDDEN -> ClientInformationPacket.ChatVisibility.HIDDEN;
            }, instance.chatColors(), instance.modelCustomisation(), switch (instance.mainHand()) {
                case LEFT -> Hand.Side.LEFT;
                case RIGHT -> Hand.Side.RIGHT;
            }, instance.textFilteringEnabled(), instance.allowsListing());
        } else if (packet instanceof ServerboundChatAckPacket instance) {
            return ChatAckPacket.create(instance.offset());
        } else if (packet instanceof ServerboundChangeDifficultyPacket instance) {
            return ChangeDifficultyPacket.create(wrap(instance.getDifficulty()));
        } else if (packet instanceof ServerboundBlockEntityTagQuery instance) {
            return BlockEntityTagQueryPacket.create(instance.getTransactionId(), wrap(instance.getPos()));
        } else if (packet instanceof ServerboundAcceptTeleportationPacket instance) {
            return AcceptTeleportationPacket.create(instance.getId());
        } else if (packet instanceof ServerboundChatPacket instance) {
            return ChatPacket.create(instance.message(), instance.timeStamp(), instance.salt(),
                    nullable(instance.signature()), wrap(instance.lastSeenMessages()));
        } else if (packet instanceof ServerboundChatCommandPacket instance) {
            return ChatCommandPacket.create(instance.command(), instance.timeStamp(), instance.salt(),
                    wrap(instance.argumentSignatures().entries()), wrap(instance.lastSeenMessages()));
        } else if (packet instanceof ServerboundClientCommandPacket instance) {
            return ClientCommandPacket.create(switch (instance.getAction()) {
                case PERFORM_RESPAWN -> ClientCommandPacket.Action.PERFORM_RESPAWN;
                case REQUEST_STATS -> ClientCommandPacket.Action.REQUEST_STATS;
            });
        } else if (packet instanceof ServerboundCustomPayloadPacket instance) {
            return CustomPayloadPacket.create(wrap(instance.getIdentifier()), wrap(instance.getData()));
        } else if (packet instanceof ServerboundInteractPacket instance) {
            return switch (instance.getActionType()) {
                case ATTACK -> InteractPacket.Attack.create(instance.getEntityId(), instance.isUsingSecondaryAction());
                case INTERACT -> {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                    instance.write(buffer);
                    int entityId = buffer.readVarInt();
                    buffer.readEnum(ServerboundInteractPacket.ActionType.class);
                    InteractionHand hand = buffer.readEnum(InteractionHand.class);
                    yield InteractPacket.Interact.create(entityId, buffer.readBoolean(), wrap(hand));
                }
                case INTERACT_AT -> {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                    instance.write(buffer);
                    int entityId = buffer.readVarInt();
                    ServerboundInteractPacket.ActionType type = buffer.readEnum(ServerboundInteractPacket.ActionType.class);
                    Vector location = new Vector(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                    InteractionHand hand = buffer.readEnum(InteractionHand.class);
                    boolean sneaking = buffer.readBoolean();
                    yield InteractPacket.InteractAt.create(entityId, sneaking, wrap(hand), location);
                }
            };
        } else if (packet instanceof ServerboundPlayerActionPacket instance) {
            return PlayerActionPacket.create(switch (instance.getAction()) {
                case START_DESTROY_BLOCK -> PlayerActionPacket.Action.START_DESTROY_BLOCK;
                case ABORT_DESTROY_BLOCK -> PlayerActionPacket.Action.ABORT_DESTROY_BLOCK;
                case STOP_DESTROY_BLOCK -> PlayerActionPacket.Action.STOP_DESTROY_BLOCK;
                case DROP_ALL_ITEMS -> PlayerActionPacket.Action.DROP_ALL_ITEMS;
                case DROP_ITEM -> PlayerActionPacket.Action.DROP_ITEM;
                case RELEASE_USE_ITEM -> PlayerActionPacket.Action.RELEASE_USE_ITEM;
                case SWAP_ITEM_WITH_OFFHAND -> PlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND;
            }, wrap(instance.getPos()), wrap(instance.getDirection()), instance.getSequence());
        } else if (packet instanceof ServerboundCommandSuggestionPacket instance) {
            return CommandSuggestionPacket.create(instance.getId(), instance.getCommand());
        } else if (packet instanceof ServerboundPlayerInputPacket instance) {
            return PlayerInputPacket.create(instance.getXxa(), instance.getZza(), instance.isJumping(), instance.isShiftKeyDown());
        } else if (packet instanceof ServerboundSignUpdatePacket instance) {
            return SignUpdatePacket.create(wrap(instance.getPos()), instance.getLines());
        } else if (packet instanceof ServerboundRenameItemPacket instance) {
            return RenameItemPacket.create(instance.getName());
        } else if (packet instanceof ServerboundContainerClickPacket instance) {
            return ContainerClickPacket.create(instance.getContainerId(), instance.getStateId(), instance.getSlotNum(),
                    instance.getButtonNum(), switch (instance.getClickType()) {
                        case PICKUP -> ContainerClickPacket.ClickType.PICKUP;
                        case QUICK_MOVE -> ContainerClickPacket.ClickType.QUICK_MOVE;
                        case SWAP -> ContainerClickPacket.ClickType.SWAP;
                        case CLONE -> ContainerClickPacket.ClickType.CLONE;
                        case THROW -> ContainerClickPacket.ClickType.THROW;
                        case QUICK_CRAFT -> ContainerClickPacket.ClickType.QUICK_CRAFT;
                        case PICKUP_ALL -> ContainerClickPacket.ClickType.PICKUP_ALL;
                    }, wrap(instance.getCarriedItem()), wrap(instance.getChangedSlots()));
        } else if (packet instanceof ServerboundPickItemPacket instance) {
            return PickItemPacket.create(instance.getSlot());
        } else if (packet instanceof ServerboundUseItemOnPacket instance) {
            return UseItemOnPacket.create(wrap(instance.getHand()), wrap(instance.getHitResult()), instance.getSequence(), instance.timestamp);
        }
        if (!unmapped.contains(packet.getClass().getName())) {
            unmapped.add(packet.getClass().getName());
            Logger.warn.println("Unmapped incoming packet: " + packet.getClass().getName());
        }
        return original.apply(packet);
    }

    private static final List<String> unmapped = new ArrayList<>();
}
