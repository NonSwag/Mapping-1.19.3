package net.nonswag.tnl.mapping.v1_19_R2.api.helper;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import net.nonswag.core.api.annotation.FieldsAreNullableByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.item.ItemHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@Getter
@FieldsAreNullableByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NMSItemHelper extends ItemHelper {
    private Map<Material, Float> compostableItems = null;

    @Override
    public void setMaxStackSize(Material material, int maxStackSize) {
        Reflection.Field.setByType(material, int.class, 1);
        Reflection.Field.setByType(Item.byId(material.ordinal()), Item.class, int.class, maxStackSize);
    }

    @Override
    public void setDurability(Material material, int durability) {
        Reflection.Field.setByType(material, short.class, (short) durability);
        Reflection.Field.setByType(Item.byId(material.ordinal()), Item.class, int.class, durability, 1);
    }

    @Override
    public Map<Material, Float> getCompostableItems() {
        if (compostableItems != null) return compostableItems;
        ImmutableMap.Builder<Material, Float> items = ImmutableMap.builder();
        Object2FloatMap<ItemLike> map = ComposterBlock.COMPOSTABLES;
        map.forEach((item, weight) -> items.put(CraftMagicNumbers.getMaterial(item.asItem()), weight));
        return compostableItems = items.build();
    }
}
