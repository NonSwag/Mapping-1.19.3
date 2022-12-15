package net.nonswag.tnl.mappings.v1_19_R3.api.helper;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.advancement.Advancement;
import net.nonswag.tnl.listener.api.gui.Interaction;
import net.nonswag.tnl.listener.api.item.SlotType;
import net.nonswag.tnl.listener.api.item.TNLItem;
import net.nonswag.tnl.listener.api.location.BlockPosition;
import net.nonswag.tnl.listener.api.nbt.NBTTag;
import net.nonswag.tnl.listener.api.packets.incoming.ChatCommandPacket;
import net.nonswag.tnl.listener.api.packets.incoming.ContainerClickPacket;
import net.nonswag.tnl.listener.api.packets.incoming.SetBeaconPacket;
import net.nonswag.tnl.listener.api.packets.incoming.UseItemOnPacket;
import net.nonswag.tnl.listener.api.packets.outgoing.*;
import net.nonswag.tnl.listener.api.player.Hand;
import net.nonswag.tnl.mappings.v1_19_R3.api.nbt.NBT;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.util.*;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class NMSHelper {

    public static SlotType wrap(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> SlotType.MAIN_HAND;
            case OFFHAND -> SlotType.OFF_HAND;
            case FEET -> SlotType.BOOTS;
            case LEGS -> SlotType.LEGGINGS;
            case CHEST -> SlotType.CHESTPLATE;
            case HEAD -> SlotType.HELMET;
        };
    }

    public static MerchantOffersPacket.Offer wrap(MerchantOffer offer) {
        MerchantOffersPacket.Offer.Builder builder = MerchantOffersPacket.Offer.builder();
        builder.baseCost(wrap(offer.getBaseCostA())).extraCost(wrap(offer.getCostB())).result(wrap(offer.getResult())).uses(offer.getUses()).
                maxUses(offer.getMaxUses()).rewardExp(offer.shouldRewardExp()).specialPrice(offer.getSpecialPriceDiff()).bonus(offer.getDemand()).
                priceMultiplier(offer.getPriceMultiplier()).xp(offer.getXp()).ignoreDiscounts(offer.ignoreDiscounts);
        return builder.build();
    }

    public static MerchantOffer wrap(MerchantOffersPacket.Offer offer) {
        MerchantOffer merchantOffer = new MerchantOffer(wrap(offer.getBaseCost()), wrap(offer.getExtraCost()), wrap(offer.getResult()), offer.getUses(),
                offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getBonus(), offer.isIgnoreDiscounts());
        merchantOffer.setSpecialPriceDiff(offer.getSpecialPrice());
        merchantOffer.rewardExp = offer.isRewardExp();
        return merchantOffer;
    }

    public static PlayerLookAtPacket.Anchor wrap(EntityAnchorArgument.Anchor anchor) {
        return switch (anchor) {
            case FEET -> PlayerLookAtPacket.Anchor.FEET;
            case EYES -> PlayerLookAtPacket.Anchor.EYES;
        };
    }

    public static EntityAnchorArgument.Anchor wrap(PlayerLookAtPacket.Anchor anchor) {
        return switch (anchor) {
            case FEET -> EntityAnchorArgument.Anchor.FEET;
            case EYES -> EntityAnchorArgument.Anchor.EYES;
        };
    }

    @Nullable
    public static PlayerLookAtPacket.Anchor nullable(@Nullable EntityAnchorArgument.Anchor anchor) {
        return anchor != null ? wrap(anchor) : null;
    }

    @Nullable
    public static EntityAnchorArgument.Anchor nullable(@Nullable PlayerLookAtPacket.Anchor anchor) {
        return anchor != null ? wrap(anchor) : null;
    }

    public static AdvancementProgress wrap(Advancement.Progress progress) {
        HashMap<String, CriterionProgress> criteriaProgress = new HashMap<>();
        progress.getProgress().forEach((name, criterionProgress) -> criteriaProgress.put(name, wrap(criterionProgress)));
        AdvancementProgress advancementProgress = new AdvancementProgress();
        Reflection.Field.setByType(advancementProgress, Map.class, criteriaProgress);
        Reflection.Field.setByType(advancementProgress, String[][].class, progress.getRequirements());
        return advancementProgress;
    }

    public static Advancement.Progress wrap(AdvancementProgress progress) {
        HashMap<String, Advancement.Criterion.Progress> criteriaProgress = new HashMap<>();
        String[][] requirements = Reflection.Field.getByType(progress, String[][].class);
        Map<String, CriterionProgress> criteria = Reflection.Field.getByType(progress, Map.class);
        criteria.forEach((name, criterionProgress) -> criteriaProgress.put(name, wrap(criterionProgress)));
        return new Advancement.Progress(criteriaProgress, requirements);
    }

    private static CriterionProgress wrap(Advancement.Criterion.Progress progress) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
        if (progress.getDateObtained() == null) return new CriterionProgress();
        return CriterionProgress.fromJson(format.format(progress.getDateObtained()));
    }

    private static Advancement.Criterion.Progress wrap(CriterionProgress progress) {
        return new Advancement.Criterion.Progress(progress.getObtained());
    }

    public static Advancement.Builder wrap(net.minecraft.advancements.Advancement.Builder builder, ResourceLocation resource) {
        net.minecraft.advancements.Advancement parent = Reflection.Field.getByType(builder, net.minecraft.advancements.Advancement.class);
        DisplayInfo display = Reflection.Field.getByType(builder, DisplayInfo.class);
        AdvancementRewards rewards = Reflection.Field.getByType(builder, AdvancementRewards.class);
        Map<String, Criterion> advancementCriteria = Reflection.Field.getByType(builder, Map.class);
        String[][] requirements = Reflection.Field.getByType(builder, String[][].class);
        RequirementsStrategy requirementsStrategy = Reflection.Field.getByType(builder, RequirementsStrategy.class);
        if (requirements == null) requirements = requirementsStrategy.createRequirements(advancementCriteria.keySet());
        HashMap<String, Advancement.Criterion<?>> criteria = new HashMap<>();
        advancementCriteria.forEach((name, criterion) -> criteria.put(name, wrap(criterion)));
        return Advancement.builder().id(wrap(resource)).parent(nullable(parent)).display(nullable(display)).
                rewards(wrap(rewards)).criteria(criteria).requirements(requirements).children(new ArrayList<>());
    }

    public static net.minecraft.advancements.Advancement wrap(Advancement advancement) {
        HashMap<String, Criterion> criteria = new HashMap<>();
        Set<net.minecraft.advancements.Advancement> children = new HashSet<>();
        advancement.getCriteria().forEach((name, criterion) -> criteria.put(name, wrap((Advancement.Criterion<SerializationContext>) criterion)));
        advancement.getChildren().forEach(child -> children.add(wrap(child)));
        net.minecraft.advancements.Advancement result = new net.minecraft.advancements.Advancement(wrap(advancement.getId()), nullable(advancement.getParent()),
                nullable(advancement.getDisplay()), wrap(advancement.getRewards()), criteria, advancement.getRequirements());
        Reflection.Field.setByType(result, Set.class, children);
        return result;
    }

    public static Advancement wrap(net.minecraft.advancements.Advancement advancement) {
        Advancement parent = nullable(advancement.getParent());
        Advancement.DisplayInfo displayInfo = nullable(advancement.getDisplay());
        Advancement.Rewards rewards = wrap(advancement.getRewards());
        HashMap<String, Advancement.Criterion<?>> criteria = new HashMap<>();
        advancement.getCriteria().forEach((name, criterion) -> criteria.put(name, wrap(criterion)));
        List<Advancement> children = new ArrayList<>();
        advancement.getChildren().forEach(child -> children.add(wrap(child)));
        return new Advancement(wrap(advancement.getId()), parent, displayInfo, rewards, criteria,
                advancement.getRequirements(), children, wrap(advancement.getChatComponent()));
    }

    private static Criterion wrap(Advancement.Criterion<SerializationContext> criterion) {
        return new Criterion(new CriterionTriggerInstance() {
            @Override
            public ResourceLocation getCriterion() {
                return wrap(criterion.getId());
            }

            @Override
            public JsonObject serializeToJson(SerializationContext predicateSerializer) {
                return criterion.serialize(predicateSerializer);
            }
        });
    }

    private static Advancement.Criterion<SerializationContext> wrap(Criterion criterion) {
        if (criterion.getTrigger() == null) throw new NullPointerException("criterion");
        return new Advancement.Criterion<>(wrap(criterion.getTrigger().getCriterion())) {
            @Override
            public JsonObject serialize(SerializationContext context) {
                return criterion.getTrigger().serializeToJson(context);
            }
        };
    }

    private static AdvancementRewards wrap(Advancement.Rewards rewards) {
        return new AdvancementRewards(rewards.getExperience(), wrap(rewards.getLoot()), wrap(rewards.getRecipes()),
                new CommandFunction.CacheableFunction(nullable(rewards.getFunction())));
    }

    private static Advancement.Rewards wrap(AdvancementRewards rewards) {
        Integer experience = Reflection.Field.getByType(rewards, int.class);
        ResourceLocation[] loot = Reflection.Field.getByType(rewards, ResourceLocation[].class);
        CommandFunction.CacheableFunction function = Reflection.Field.getByType(rewards, CommandFunction.CacheableFunction.class);
        return new Advancement.Rewards(experience, wrap(loot), wrap(rewards.getRecipes()), nullable(function.getId()));
    }

    @Nullable
    public static net.minecraft.advancements.Advancement nullable(@Nullable Advancement advancement) {
        return advancement != null ? wrap(advancement) : null;
    }

    @Nullable
    public static Advancement nullable(@Nullable net.minecraft.advancements.Advancement advancement) {
        return advancement != null ? wrap(advancement) : null;
    }

    public static Advancement.DisplayInfo wrap(DisplayInfo displayInfo) {
        return new Advancement.DisplayInfo(wrap(displayInfo.getIcon()), wrap(displayInfo.getTitle()), wrap(displayInfo.getDescription()),
                nullable(displayInfo.getBackground()), wrap(displayInfo.getFrame()), displayInfo.shouldShowToast(),
                displayInfo.shouldAnnounceChat(), displayInfo.isHidden(), displayInfo.getX(), displayInfo.getY());
    }

    public static DisplayInfo wrap(Advancement.DisplayInfo displayInfo) {
        DisplayInfo info = new DisplayInfo(wrap(displayInfo.getIcon()), wrap(displayInfo.getTitle()), wrap(displayInfo.getDescription()),
                nullable(displayInfo.getBackground()), wrap(displayInfo.getFrame()), displayInfo.isShowToast(),
                displayInfo.isAnnounceChat(), displayInfo.isHidden());
        info.setLocation(displayInfo.getX(), displayInfo.getY());
        return info;
    }

    @Nullable
    public static DisplayInfo nullable(@Nullable Advancement.DisplayInfo displayInfo) {
        return displayInfo != null ? wrap(displayInfo) : null;
    }

    @Nullable
    public static Advancement.DisplayInfo nullable(@Nullable DisplayInfo displayInfo) {
        return displayInfo != null ? wrap(displayInfo) : null;
    }

    private static FrameType wrap(Advancement.FrameType frame) {
        return switch (frame) {
            case GOAL -> FrameType.GOAL;
            case TASK -> FrameType.TASK;
            case CHALLENGE -> FrameType.CHALLENGE;
        };
    }

    private static Advancement.FrameType wrap(FrameType frame) {
        return switch (frame) {
            case GOAL -> Advancement.FrameType.GOAL;
            case TASK -> Advancement.FrameType.TASK;
            case CHALLENGE -> Advancement.FrameType.CHALLENGE;
        };
    }

    public static OpenScreenPacket.Type wrap(MenuType<?> type) {
        if (type.equals(MenuType.GENERIC_9x1)) return OpenScreenPacket.Type.CHEST_9X1;
        else if (type.equals(MenuType.GENERIC_9x2)) return OpenScreenPacket.Type.CHEST_9X2;
        else if (type.equals(MenuType.GENERIC_9x3)) return OpenScreenPacket.Type.CHEST_9X3;
        else if (type.equals(MenuType.GENERIC_9x4)) return OpenScreenPacket.Type.CHEST_9X4;
        else if (type.equals(MenuType.GENERIC_9x5)) return OpenScreenPacket.Type.CHEST_9X5;
        else if (type.equals(MenuType.GENERIC_9x6)) return OpenScreenPacket.Type.CHEST_9X6;
        else if (type.equals(MenuType.GENERIC_3x3)) return OpenScreenPacket.Type.DISPENSER;
        else if (type.equals(MenuType.ANVIL)) return OpenScreenPacket.Type.ANVIL;
        else if (type.equals(MenuType.BEACON)) return OpenScreenPacket.Type.BEACON;
        else if (type.equals(MenuType.BLAST_FURNACE)) return OpenScreenPacket.Type.BLAST_FURNACE;
        else if (type.equals(MenuType.BREWING_STAND)) return OpenScreenPacket.Type.BREWING_STAND;
        else if (type.equals(MenuType.CRAFTING)) return OpenScreenPacket.Type.WORKBENCH;
        else if (type.equals(MenuType.ENCHANTMENT)) return OpenScreenPacket.Type.ENCHANTER;
        else if (type.equals(MenuType.FURNACE)) return OpenScreenPacket.Type.FURNACE;
        else if (type.equals(MenuType.GRINDSTONE)) return OpenScreenPacket.Type.GRINDSTONE;
        else if (type.equals(MenuType.HOPPER)) return OpenScreenPacket.Type.HOPPER;
        else if (type.equals(MenuType.LECTERN)) return OpenScreenPacket.Type.LECTERN;
        else if (type.equals(MenuType.LOOM)) return OpenScreenPacket.Type.LOOM;
        else if (type.equals(MenuType.MERCHANT)) return OpenScreenPacket.Type.MERCHANT;
        else if (type.equals(MenuType.SHULKER_BOX)) return OpenScreenPacket.Type.SHULKER_BOX;
        else if (type.equals(MenuType.SMITHING)) return OpenScreenPacket.Type.SMITHING_TABLE;
        else if (type.equals(MenuType.SMOKER)) return OpenScreenPacket.Type.SMOKER;
        else if (type.equals(MenuType.CARTOGRAPHY_TABLE)) return OpenScreenPacket.Type.CARTOGRAPHY_TABLE;
        else if (type.equals(MenuType.STONECUTTER)) return OpenScreenPacket.Type.STONECUTTER;
        else throw new IllegalStateException("unknown menu type: " + type.getClass().getName());
    }

    public static MenuType<?> wrap(OpenScreenPacket.Type type) {
        return switch (type) {
            case CHEST_9X1 -> MenuType.GENERIC_9x1;
            case CHEST_9X2 -> MenuType.GENERIC_9x2;
            case CHEST_9X3 -> MenuType.GENERIC_9x3;
            case CHEST_9X4 -> MenuType.GENERIC_9x4;
            case CHEST_9X5 -> MenuType.GENERIC_9x5;
            case CHEST_9X6 -> MenuType.GENERIC_9x6;
            case DISPENSER -> MenuType.GENERIC_3x3;
            case ANVIL -> MenuType.ANVIL;
            case BEACON -> MenuType.BEACON;
            case BLAST_FURNACE -> MenuType.BLAST_FURNACE;
            case BREWING_STAND -> MenuType.BREWING_STAND;
            case WORKBENCH -> MenuType.CRAFTING;
            case ENCHANTER -> MenuType.ENCHANTMENT;
            case FURNACE -> MenuType.FURNACE;
            case GRINDSTONE -> MenuType.GRINDSTONE;
            case HOPPER -> MenuType.HOPPER;
            case LECTERN -> MenuType.LECTERN;
            case LOOM -> MenuType.LOOM;
            case MERCHANT -> MenuType.MERCHANT;
            case SHULKER_BOX -> MenuType.SHULKER_BOX;
            case SMITHING_TABLE -> MenuType.SMITHING;
            case SMOKER -> MenuType.SMOKER;
            case CARTOGRAPHY_TABLE -> MenuType.CARTOGRAPHY_TABLE;
            case STONECUTTER -> MenuType.STONECUTTER;
        };
    }

    public static byte[] wrap(FriendlyByteBuf buffer) {
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        return data;
    }

    @Nullable
    public static net.kyori.adventure.text.Component nullable(@Nullable Component component) {
        return component != null ? wrap(component) : null;
    }

    @Nullable
    public static Component nullable(@Nullable net.kyori.adventure.text.Component component) {
        return component != null ? wrap(component) : null;
    }

    @Nonnull
    public static net.kyori.adventure.text.Component wrap(@Nonnull Component component) {
        return PaperAdventure.asAdventure(component);
    }

    @Nonnull
    public static Component wrap(@Nonnull net.kyori.adventure.text.Component component) {
        return PaperAdventure.asVanilla(component);
    }

    public static EntityType wrap(net.minecraft.world.entity.EntityType<?> type) {
        return CraftMagicNumbers.getEntityType(type);
    }

    public static net.minecraft.world.entity.EntityType<?> wrap(EntityType type) {
        return CraftMagicNumbers.getEntityTypes(type);
    }

    public static Material wrap(Item item) {
        return CraftMagicNumbers.getMaterial(item);
    }

    public static Item wrap(Material material) {
        return CraftMagicNumbers.getItem(material);
    }

    public static Difficulty wrap(net.minecraft.world.Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> Difficulty.PEACEFUL;
            case EASY -> Difficulty.EASY;
            case NORMAL -> Difficulty.NORMAL;
            case HARD -> Difficulty.HARD;
        };
    }

    public static net.minecraft.world.Difficulty wrap(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> net.minecraft.world.Difficulty.PEACEFUL;
            case EASY -> net.minecraft.world.Difficulty.EASY;
            case NORMAL -> net.minecraft.world.Difficulty.NORMAL;
            case HARD -> net.minecraft.world.Difficulty.HARD;
        };
    }

    public static NBTTag wrap(CompoundTag tag) {
        return new NBT(tag);
    }

    @Nullable
    public static NBTTag nullable(@Nullable CompoundTag tag) {
        return tag != null ? wrap(tag) : null;
    }

    public static ChatColor wrap(ChatFormatting formatting) {
        return CraftChatMessage.getColor(formatting);
    }

    public static ChatFormatting wrap(ChatColor color) {
        return CraftChatMessage.getColor(color);
    }

    public static SetPlayerTeamPacket.Parameters wrap(ClientboundSetPlayerTeamPacket.Parameters parameters) {
        SetPlayerTeamPacket.Parameters value = new SetPlayerTeamPacket.Parameters();
        value.setDisplayName(parameters.getDisplayName().getString());
        value.setNameTagVisibility(switch (parameters.getNametagVisibility()) {
            case "never" -> SetPlayerTeamPacket.Parameters.Visibility.NEVER;
            case "hideForOtherTeams" -> SetPlayerTeamPacket.Parameters.Visibility.HIDE_FOR_OTHER_TEAMS;
            case "hideForOwnTeam" -> SetPlayerTeamPacket.Parameters.Visibility.HIDE_FOR_OWN_TEAM;
            default -> SetPlayerTeamPacket.Parameters.Visibility.ALWAYS;
        });
        value.setCollisionRule(switch (parameters.getCollisionRule()) {
            case "never" -> SetPlayerTeamPacket.Parameters.CollisionRule.NEVER;
            case "pushOtherTeams" -> SetPlayerTeamPacket.Parameters.CollisionRule.PUSH_OTHER_TEAMS;
            case "pushOwnTeam" -> SetPlayerTeamPacket.Parameters.CollisionRule.PUSH_OWN_TEAM;
            default -> SetPlayerTeamPacket.Parameters.CollisionRule.ALWAYS;
        });
        value.setColor(wrap(parameters.getColor()));
        value.setPrefix(parameters.getPlayerPrefix().getString());
        value.setSuffix(parameters.getPlayerSuffix().getString());
        value.unpackOptions(parameters.getOptions());
        return value;
    }

    public static EquipmentSlot wrap(SlotType type) {
        return switch (type) {
            case MAIN_HAND -> EquipmentSlot.MAINHAND;
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }

    public static ClientboundGameEventPacket.Type wrap(GameEventPacket.Identifier identifier) {
        return switch (identifier.getId()) {
            case 0 -> ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE;
            case 1 -> ClientboundGameEventPacket.START_RAINING;
            case 2 -> ClientboundGameEventPacket.STOP_RAINING;
            case 3 -> ClientboundGameEventPacket.CHANGE_GAME_MODE;
            case 4 -> ClientboundGameEventPacket.WIN_GAME;
            case 5 -> ClientboundGameEventPacket.DEMO_EVENT;
            case 6 -> ClientboundGameEventPacket.ARROW_HIT_PLAYER;
            case 7 -> ClientboundGameEventPacket.RAIN_LEVEL_CHANGE;
            case 8 -> ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE;
            case 9 -> ClientboundGameEventPacket.PUFFER_FISH_STING;
            case 10 -> ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT;
            case 11 -> ClientboundGameEventPacket.IMMEDIATE_RESPAWN;
            default -> throw new IllegalStateException("Unexpected value: " + identifier.getId());
        };
    }

    public static GameEventPacket.Identifier wrap(ClientboundGameEventPacket.Type type) {
        if (type.equals(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE)) {
            return GameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE;
        } else if (type.equals(ClientboundGameEventPacket.START_RAINING)) {
            return GameEventPacket.START_RAINING;
        } else if (type.equals(ClientboundGameEventPacket.STOP_RAINING)) {
            return GameEventPacket.STOP_RAINING;
        } else if (type.equals(ClientboundGameEventPacket.CHANGE_GAME_MODE)) {
            return GameEventPacket.CHANGE_GAMEMODE;
        } else if (type.equals(ClientboundGameEventPacket.WIN_GAME)) {
            return GameEventPacket.WIN_GAME;
        } else if (type.equals(ClientboundGameEventPacket.DEMO_EVENT)) {
            return GameEventPacket.DEMO_EVENT;
        } else if (type.equals(ClientboundGameEventPacket.ARROW_HIT_PLAYER)) {
            return GameEventPacket.ARROW_HIT_PLAYER;
        } else if (type.equals(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE)) {
            return GameEventPacket.RAIN_LEVEL_CHANGE;
        } else if (type.equals(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE)) {
            return GameEventPacket.THUNDER_LEVEL_CHANGE;
        } else if (type.equals(ClientboundGameEventPacket.PUFFER_FISH_STING)) {
            return GameEventPacket.PUFFER_FISH_STING;
        } else if (type.equals(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT)) {
            return GameEventPacket.GUARDIAN_ELDER_EFFECT;
        } else if (type.equals(ClientboundGameEventPacket.IMMEDIATE_RESPAWN)) {
            return GameEventPacket.IMMEDIATE_RESPAWN;
        } else throw new IllegalStateException("Unmapped game event type");
    }

    public static ClientboundPlayerInfoPacket.Action wrap(PlayerInfoPacket.Action action) {
        return switch (action) {
            case ADD_PLAYER -> ClientboundPlayerInfoPacket.Action.ADD_PLAYER;
            case REMOVE_PLAYER -> ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER;
            case UPDATE_LATENCY -> ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY;
            case UPDATE_GAME_MODE -> ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE;
            case UPDATE_DISPLAY_NAME -> ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME;
        };
    }

    public static SoundSource wrap(SoundCategory category) {
        return switch (category) {
            case MASTER -> SoundSource.MASTER;
            case MUSIC -> SoundSource.MUSIC;
            case RECORDS -> SoundSource.RECORDS;
            case WEATHER -> SoundSource.WEATHER;
            case BLOCKS -> SoundSource.BLOCKS;
            case HOSTILE -> SoundSource.HOSTILE;
            case NEUTRAL -> SoundSource.NEUTRAL;
            case PLAYERS -> SoundSource.PLAYERS;
            case AMBIENT -> SoundSource.AMBIENT;
            case VOICE -> SoundSource.VOICE;
        };
    }

    @Nullable
    public static SoundSource nullable(@Nullable SoundCategory category) {
        return category != null ? wrap(category) : null;
    }

    public static SoundCategory wrap(SoundSource source) {
        return switch (source) {
            case MASTER -> SoundCategory.MASTER;
            case MUSIC -> SoundCategory.MUSIC;
            case RECORDS -> SoundCategory.RECORDS;
            case WEATHER -> SoundCategory.WEATHER;
            case BLOCKS -> SoundCategory.BLOCKS;
            case HOSTILE -> SoundCategory.HOSTILE;
            case NEUTRAL -> SoundCategory.NEUTRAL;
            case PLAYERS -> SoundCategory.PLAYERS;
            case AMBIENT -> SoundCategory.AMBIENT;
            case VOICE -> SoundCategory.VOICE;
        };
    }

    @Nullable
    public static SoundCategory nullable(@Nullable SoundSource source) {
        return source != null ? wrap(source) : null;
    }

    public static CommandSuggestionsPacket.Suggestions wrap(Suggestions suggestions) {
        List<CommandSuggestionsPacket.Suggestion> list = new ArrayList<>();
        suggestions.getList().forEach(suggestion -> list.add(wrap(suggestion)));
        return new CommandSuggestionsPacket.Suggestions(wrap(suggestions.getRange()), list);
    }

    public static CommandSuggestionsPacket.StringRange wrap(StringRange range) {
        return new CommandSuggestionsPacket.StringRange(range.getStart(), range.getEnd());
    }

    public static CommandSuggestionsPacket.Suggestion wrap(Suggestion suggestion) {
        return new CommandSuggestionsPacket.Suggestion(wrap(suggestion.getRange()), suggestion.getText(), nullable(suggestion.getTooltip()));
    }

    public static CommandSuggestionsPacket.Tooltip wrap(Message message) {
        return message::getString;
    }

    @Nullable
    public static CommandSuggestionsPacket.Tooltip nullable(@Nullable Message message) {
        return message != null ? wrap(message) : null;
    }

    public static Suggestions wrap(CommandSuggestionsPacket.Suggestions suggestions) {
        List<Suggestion> list = new ArrayList<>();
        suggestions.getSuggestions().forEach(suggestion -> list.add(wrap(suggestion)));
        return new Suggestions(wrap(suggestions.getRange()), list);
    }

    public static StringRange wrap(CommandSuggestionsPacket.StringRange range) {
        return new StringRange(range.start(), range.end());
    }

    public static Suggestion wrap(CommandSuggestionsPacket.Suggestion suggestion) {
        return new Suggestion(wrap(suggestion.getRange()), suggestion.getText(), nullable(suggestion.getTooltip()));
    }

    public static Message wrap(CommandSuggestionsPacket.Tooltip tooltip) {
        return tooltip::getMessage;
    }

    @Nullable
    public static Message nullable(@Nullable CommandSuggestionsPacket.Tooltip tooltip) {
        return tooltip != null ? wrap(tooltip) : null;
    }

    public static Vec3i wrap(Vector vector) {
        return new Vec3i(vector.getX(), vector.getY(), vector.getZ());
    }

    public static ItemStack wrap(TNLItem item) {
        return CraftItemStack.asNMSCopy(item);
    }

    @Nullable
    public static ItemStack nullable(@Nullable TNLItem item) {
        return item != null ? wrap(item) : null;
    }

    @Nullable
    public static MobEffect nullable(@Nullable SetBeaconPacket.Effect effect) {
        return effect != null ? MobEffect.byId(effect.id()) : null;
    }

    @Nullable
    public static ResourceLocation nullable(@Nullable NamespacedKey namespacedKey) {
        return namespacedKey != null ? wrap(namespacedKey) : null;
    }

    public static ResourceLocation wrap(NamespacedKey channel) {
        return new ResourceLocation(channel.getNamespace(), channel.getKey());
    }

    public static ArgumentSignatures wrap(ChatCommandPacket.Entry[] argumentSignatures) {
        List<ArgumentSignatures.Entry> entries = new ArrayList<>();
        for (ChatCommandPacket.Entry signature : argumentSignatures) {
            entries.add(new ArgumentSignatures.Entry(signature.getName(), new MessageSignature(signature.getSignature())));
        }
        return new ArgumentSignatures(entries);
    }

    public static net.nonswag.tnl.listener.api.chat.LastSeenMessages.Update wrap(LastSeenMessages.Update lastSeenMessages) {
        List<net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry> entries = new ArrayList<>();
        lastSeenMessages.lastSeen().entries().forEach(entry -> entries.add(wrap(entry)));
        return new net.nonswag.tnl.listener.api.chat.LastSeenMessages.Update(new net.nonswag.tnl.listener.api.chat.LastSeenMessages(entries), nullable(lastSeenMessages.lastReceived().orElse(null)));
    }

    public static net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry wrap(LastSeenMessages.Entry entry) {
        return new net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry(entry.profileId(), entry.lastSignature().bytes());
    }

    @Nullable
    public static net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry nullable(@Nullable LastSeenMessages.Entry entry) {
        return entry != null ? wrap(entry) : null;
    }

    public static LastSeenMessages.Update wrap(net.nonswag.tnl.listener.api.chat.LastSeenMessages.Update lastSeenMessages) {
        List<LastSeenMessages.Entry> entries = new ArrayList<>();
        lastSeenMessages.getLastSeen().getEntries().forEach(entry -> entries.add(wrap(entry)));
        return new LastSeenMessages.Update(new LastSeenMessages(entries), Optional.ofNullable(nullable(lastSeenMessages.getLastReceived())));
    }

    public static LastSeenMessages.Entry wrap(net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry entry) {
        return new LastSeenMessages.Entry(entry.profileId(), new MessageSignature(entry.lastSignature()));
    }

    @Nullable
    public static LastSeenMessages.Entry nullable(@Nullable net.nonswag.tnl.listener.api.chat.LastSeenMessages.Entry entry) {
        return entry != null ? wrap(entry) : null;
    }

    public static BlockPos wrap(BlockPosition position) {
        return new BlockPos(position.getX(), position.getY(), position.getZ());
    }

    public static UseItemOnPacket.BlockTargetResult wrap(BlockHitResult result) {
        return new UseItemOnPacket.BlockTargetResult(result.getType().equals(HitResult.Type.MISS),
                wrap(result.getLocation()), wrap(result.getBlockPos()), wrap(result.getDirection()), result.isInside());
    }

    public static BlockHitResult wrap(UseItemOnPacket.BlockTargetResult result) {
        Vec3 location = new Vec3(result.getLocation().getX(), result.getLocation().getY(), result.getLocation().getZ());
        if (result.isMissed()) return BlockHitResult.miss(location, wrap(result.getSide()), wrap(result.getPosition()));
        return new BlockHitResult(location, wrap(result.getSide()), wrap(result.getPosition()), true);
    }

    public static Direction wrap(net.nonswag.tnl.listener.api.location.Direction direction) {
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
        };
    }

    public static net.nonswag.tnl.listener.api.location.Direction wrap(Direction direction) {
        return switch (direction) {
            case UP -> net.nonswag.tnl.listener.api.location.Direction.UP;
            case DOWN -> net.nonswag.tnl.listener.api.location.Direction.DOWN;
            case NORTH -> net.nonswag.tnl.listener.api.location.Direction.NORTH;
            case SOUTH -> net.nonswag.tnl.listener.api.location.Direction.SOUTH;
            case EAST -> net.nonswag.tnl.listener.api.location.Direction.EAST;
            case WEST -> net.nonswag.tnl.listener.api.location.Direction.WEST;
        };
    }

    public static Hand wrap(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> Hand.MAIN_HAND;
            case OFF_HAND -> Hand.OFF_HAND;
        };
    }

    public static InteractionHand wrap(Hand hand) {
        return switch (hand) {
            case MAIN_HAND -> InteractionHand.MAIN_HAND;
            case OFF_HAND -> InteractionHand.OFF_HAND;
        };
    }

    public static BlockPos wrap(Location location) {
        return new BlockPos(location.getX(), location.getY(), location.getZ());
    }

    @Nullable
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static SetBeaconPacket.Effect wrap(Optional<MobEffect> optional) {
        if (optional.isEmpty()) return null;
        return new SetBeaconPacket.Effect(switch (optional.get().getCategory()) {
            case HARMFUL -> SetBeaconPacket.Effect.Category.HARMFUL;
            case NEUTRAL -> SetBeaconPacket.Effect.Category.NEUTRAL;
            case BENEFICIAL -> SetBeaconPacket.Effect.Category.BENEFICIAL;
        }, optional.get().getColor(), MobEffect.getId(optional.get()));
    }

    public static TNLItem wrap(ItemStack item) {
        return TNLItem.create(CraftItemStack.asBukkitCopy(item));
    }

    @Nullable
    public static TNLItem nullable(@Nullable ItemStack item) {
        return item != null ? wrap(item) : null;
    }

    public static ResourceLocation[] wrap(NamespacedKey[] keys) {
        ResourceLocation[] resources = new ResourceLocation[keys.length];
        for (int i = 0; i < keys.length; i++) resources[i] = wrap(keys[i]);
        return resources;
    }

    public static NamespacedKey[] wrap(ResourceLocation[] resources) {
        NamespacedKey[] keys = new NamespacedKey[resources.length];
        for (int i = 0; i < resources.length; i++) keys[i] = wrap(resources[i]);
        return keys;
    }

    public static NamespacedKey wrap(ResourceLocation resource) {
        return new NamespacedKey(resource.getNamespace(), resource.getPath());
    }

    @Nullable
    public static NamespacedKey nullable(@Nullable ResourceLocation resource) {
        return resource != null ? wrap(resource) : null;
    }

    public static HashMap<Integer, TNLItem> wrap(Int2ObjectMap<ItemStack> items) {
        HashMap<Integer, TNLItem> result = new HashMap<>();
        items.forEach((integer, itemStack) -> result.put(integer, wrap(itemStack)));
        return result;
    }

    public static BlockFace wrap(net.nonswag.tnl.listener.api.location.Direction direction, int dummy) {
        return switch (direction) {
            case DOWN -> BlockFace.DOWN;
            case UP -> BlockFace.UP;
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case WEST -> BlockFace.WEST;
            case EAST -> BlockFace.EAST;
        };
    }

    public static Interaction.Type wrap(int buttonId, ContainerClickPacket.ClickType clickType) {
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

    public static List<TNLItem> wrap(List<ItemStack> items, int dummy) {
        List<TNLItem> result = new ArrayList<>();
        items.forEach(itemStack -> result.add(wrap(itemStack)));
        return result;
    }

    public static Int2ObjectMap<ItemStack> wrap(HashMap<Integer, TNLItem> changedSlots) {
        Int2ObjectMap<ItemStack> result = new Int2ObjectOpenHashMap<>();
        changedSlots.forEach((integer, itemStack) -> result.put((int) integer, wrap(itemStack)));
        return result;
    }

    public static ChatCommandPacket.Entry[] wrap(List<ArgumentSignatures.Entry> entries) {
        ChatCommandPacket.Entry[] signature = new ChatCommandPacket.Entry[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            ArgumentSignatures.Entry entry = entries.get(i);
            signature[i] = new ChatCommandPacket.Entry(entry.name(), entry.signature().bytes());
        }
        return signature;
    }

    public static Vector wrap(Vec3i vec3i) {
        return new Vector(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static Vector wrap(Vec3 vec3) {
        return new Vector(vec3.x(), vec3.y(), vec3.z());
    }

    public static BlockPosition wrap(BlockPos pos) {
        return new BlockPosition(pos.getX(), pos.getY(), pos.getZ());
    }
}
