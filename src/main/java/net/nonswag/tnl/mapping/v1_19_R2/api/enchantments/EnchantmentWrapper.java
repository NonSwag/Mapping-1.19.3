package net.nonswag.tnl.mapping.v1_19_R2.api.enchantments;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.nonswag.tnl.listener.api.enchantment.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@Getter
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnchantmentWrapper extends Enchant {
    private final Enchantment vanilla;
    private final NamespacedKey namespace;

    public EnchantmentWrapper(NamespacedKey key, String name, EnchantmentTarget target) {
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

            @Override
            public Component getFullname(int level) {
                return Component.literal(EnchantmentWrapper.this.getName());
            }

            @Override
            public boolean canEnchant(ItemStack itemStack) {
                return EnchantmentWrapper.this.canEnchantItem(CraftItemStack.asBukkitCopy(itemStack));
            }

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

    @Override
    protected Enchant register() {
        // Registry.register(BuiltInRegistries.ENCHANTMENT, getNamespace().getKey(), getVanilla());
        return super.register();
    }

    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public float getDamageIncrease(int level, EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return Set.of(org.bukkit.inventory.EquipmentSlot.values());
    }

    public String translationKey() {
        return "enchantment.unknown";
    }

    public Key key() {
        return namespace;
    }
}
