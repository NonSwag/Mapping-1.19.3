package net.nonswag.tnl.mappings.v1_19_R3.api.enchantments;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.enchantment.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;

import javax.annotation.Nonnull;
import java.util.Set;

@Getter
public class EnchantmentWrapper extends Enchant {

    @Nonnull
    private final Enchantment vanilla;
    @Nonnull
    private final NamespacedKey namespace;

    public EnchantmentWrapper(@Nonnull NamespacedKey key, @Nonnull String name, @Nonnull EnchantmentTarget target) {
        super(key, name, target);
        this.namespace = key;
        this.vanilla = new Enchantment(Enchantment.Rarity.COMMON, EnchantmentCategory.ARMOR, EquipmentSlot.values()) {
            @Override
            public int getMaxLevel() {
                return EnchantmentWrapper.this.getMaxLevel();
            }

            @Override
            public int getMinLevel() {
                return EnchantmentWrapper.this.getStartLevel();
            }

            @Override
            public boolean isTreasureOnly() {
                return EnchantmentWrapper.this.isTreasure();
            }

            @Override
            public boolean isCurse() {
                return EnchantmentWrapper.this.isCursed();
            }

            @Nonnull
            @Override
            public Component getFullname(int level) {
                return Component.literal(EnchantmentWrapper.this.getName());
            }

            @Override
            public boolean canEnchant(@Nonnull ItemStack itemStack) {
                return EnchantmentWrapper.this.canEnchantItem(CraftItemStack.asBukkitCopy(itemStack));
            }

            @Nonnull
            @Override
            public Rarity getRarity() {
                return switch (getRarity()) {
                    case COMMON -> Rarity.COMMON;
                    case UNCOMMON -> Rarity.UNCOMMON;
                    case RARE -> Rarity.RARE;
                    case VERY_RARE -> Rarity.VERY_RARE;
                };
            }
        };
        this.register();
    }

    @Nonnull
    @Override
    protected Enchant register() {
        // Reflection.Field.set(Registry.ENCHANTMENT, MappedRegistry.class, "ca", false); // set boolean "frozen" to false
        Reflection.Field.setByType(Registry.ENCHANTMENT, MappedRegistry.class, boolean.class, false);
        Registry.register(Registry.ENCHANTMENT, getNamespace().getKey(), getVanilla());
        return super.register();
    }

    @Nonnull
    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public float getDamageIncrease(int level, @Nonnull EntityCategory entityCategory) {
        return 0;
    }

    @Nonnull
    @Override
    public Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return Set.of(org.bukkit.inventory.EquipmentSlot.values());
    }

    @Nonnull
    public String translationKey() {
        return "enchantment.unknown";
    }

    @Nonnull
    public Key key() {
        return namespace;
    }
}
