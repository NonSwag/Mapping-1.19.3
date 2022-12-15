package net.nonswag.tnl.mappings.v1_19_R3.api.helper;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.item.ItemHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Getter
public class NMSItemHelper extends ItemHelper {

    @Nullable
    private Map<Material, Float> compostableItems = null;

    @Override
    public void setMaxStackSize(@Nonnull Material material, int maxStackSize) {
        Reflection.Field.set(material, "maxStack", maxStackSize);
        Reflection.Field.set(Item.byId(material.ordinal()), Item.class, "maxStackSize", maxStackSize);
    }

    @Override
    public void setDurability(@Nonnull Material material, int durability) {
        Reflection.Field.set(material, "durability", durability);
        Reflection.Field.set(Item.byId(material.ordinal()), Item.class, "durability", durability);
    }

    @Nonnull
    @Override
    public Map<Material, Float> getCompostableItems() {
        if (compostableItems != null) return compostableItems;
        ImmutableMap.Builder<Material, Float> items = ImmutableMap.builder();
        Object2FloatMap<ItemLike> map = ComposterBlock.COMPOSTABLES;
        map.forEach((item, weight) -> items.put(CraftMagicNumbers.getMaterial(item.asItem()), weight));
        return compostableItems = items.build();
    }
}
