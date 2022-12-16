package net.nonswag.tnl.mappings.v1_19_R2.api.entity;

import net.nonswag.tnl.listener.api.entity.EntityHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

public class NMSEntityHelper extends EntityHelper {
    @Nullable
    @Override
    public Entity getEntity(int id) {
        for (World world : Bukkit.getWorlds()) {
            net.minecraft.world.entity.Entity entity = ((CraftWorld) world).getHandle().getEntityLookup().get(id);
            if (entity != null) return entity.getBukkitEntity();
        }
        return null;
    }
}
