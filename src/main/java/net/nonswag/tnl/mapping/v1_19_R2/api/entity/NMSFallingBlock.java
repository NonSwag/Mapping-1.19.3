package net.nonswag.tnl.mapping.v1_19_R2.api.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.nonswag.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.entity.TNLEntity;
import net.nonswag.tnl.listener.api.entity.TNLFallingBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class NMSFallingBlock extends FallingBlockEntity implements TNLFallingBlock {

    public NMSFallingBlock(Location location, Material type) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), ((CraftBlockData) type.createBlockData()).getState());
        this.blocksBuilding = false;
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
    }

    @Override
    public void setType(Material type) {
        Reflection.Field.set(this, "blockState", ((CraftBlockData) type.createBlockData()).getState());
    }

    @Override
    public void setGlowing(boolean glowing) {
        setGlowingTag(glowing);
    }

    @Override
    public boolean teleport(Location location) {
        moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return true;
    }

    @Override
    public boolean teleport(Entity entity) {
        return teleport(entity.getLocation());
    }

    @Override
    public boolean teleport(TNLEntity entity) {
        return teleport(entity.bukkit());
    }

    @Override
    public void setCustomName(String customName) {
        setCustomName(Component.literal(customName));
        setCustomNameVisible(true);
    }

    @Override
    public int getEntityId() {
        return super.getId();
    }

    @Override
    public CraftEntity bukkit() {
        return super.getBukkitEntity();
    }
}
