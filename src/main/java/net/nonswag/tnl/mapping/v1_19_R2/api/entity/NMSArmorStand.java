package net.nonswag.tnl.mapping.v1_19_R2.api.entity;

import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.nonswag.tnl.listener.api.entity.TNLArmorStand;
import net.nonswag.tnl.listener.api.item.SlotType;
import net.nonswag.tnl.listener.api.item.TNLItem;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;

import javax.annotation.Nullable;

import static net.nonswag.tnl.mapping.v1_19_R2.api.helper.NMSHelper.wrap;

public class NMSArmorStand implements TNLArmorStand {

    private final ArmorStand armorStand;

    public NMSArmorStand(World world, double x, double y, double z, float yaw, float pitch) {
        this.armorStand = new ArmorStand(((CraftWorld) world).getHandle(), x, y, z);
        this.armorStand.setRot(yaw, pitch);
    }

    @Override
    public void setX(double x) {
        armorStand.moveTo(x, armorStand.position().y(), armorStand.position().z());
    }

    @Override
    public void setY(double y) {
        armorStand.moveTo(armorStand.position().x(), y, armorStand.position().z());
    }

    @Override
    public void setZ(double z) {
        armorStand.moveTo(armorStand.position().x(), armorStand.position().y(), z);
    }

    @Override
    public void updateSize() {
    }

    @Override
    public boolean doAITick() {
        return armorStand.isEffectiveAi();
    }

    @Override
    public void setHeadRotation(float rotation) {
        armorStand.setYHeadRot(rotation);
    }

    @Override
    public void tick() {
        armorStand.tick();
    }

    @Override
    public boolean isBaby() {
        return armorStand.isBaby();
    }

    @Override
    public void killEntity() {
        armorStand.remove(Entity.RemovalReason.KILLED);
    }

    @Override
    public void setSmall(boolean flag) {
        armorStand.setSmall(flag);
    }

    @Override
    public boolean isSmall() {
        return armorStand.isSmall();
    }

    @Override
    public void setArms(boolean arms) {
        armorStand.setShowArms(arms);
    }

    @Override
    public boolean hasArms() {
        return armorStand.isShowArms();
    }

    @Override
    public void setHeadPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setHeadPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public void setBodyPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setBodyPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public void setLeftArmPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setLeftArmPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public void setRightArmPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setRightArmPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public void setLeftLegPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setLeftLegPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public void setRightLegPose(@Nullable Pose pose) {
        if (pose != null) armorStand.setRightLegPose(new Rotations(pose.getPitch(), pose.getYaw(), pose.getRoll()));
    }

    @Override
    public boolean isInteractable() {
        return false;
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        armorStand.setCustomNameVisible(flag);
    }

    @Override
    public void setCustomName(@Nullable String customName) {
        armorStand.setCustomName(customName != null ? Component.literal(customName) : null);
    }

    @Override
    public void setVisible(boolean visible) {
        armorStand.setInvisible(!visible);
    }

    @Override
    public void setInvulnerable(boolean invulnerable) {
        armorStand.setInvulnerable(invulnerable);
    }

    @Override
    public void setGravity(boolean gravity) {
        armorStand.setNoGravity(!gravity);
    }

    @Override
    public void setBasePlate(boolean flag) {
        armorStand.setNoBasePlate(!flag);
    }

    @Override
    public boolean hasBasePlate() {
        return !armorStand.isNoBasePlate();
    }

    @Override
    public void setMarker(boolean flag) {
        armorStand.setMarker(flag);
    }

    @Override
    public boolean isMarker() {
        return armorStand.isMarker();
    }

    @Override
    public void setItemInMainHand(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public void setItemInOffHand(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public void setHelmet(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public void setChestplate(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public void setLeggings(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public void setBoots(@Nullable TNLItem item) {
        armorStand.setItemSlot(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(item != null ? item.getItemStack() : null), true);
    }

    @Override
    public SynchedEntityData getDataWatcher() {
        return armorStand.getEntityData();
    }

    @Override
    public void setLocation(Location location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void setLocation(double x, double y, double z) {
        armorStand.moveTo(x, y, z);
    }

    @Override
    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        armorStand.moveTo(x, y, z, yaw, pitch);
    }

    @Override
    public void setItem(SlotType slot, TNLItem item) {
        armorStand.setItemSlot(wrap(slot), CraftItemStack.asNMSCopy(item.getItemStack()), true);
    }

    @Override
    public int getEntityId() {
        return armorStand.getId();
    }

    @Override
    public CraftLivingEntity bukkit() {
        return (CraftLivingEntity) armorStand.getBukkitEntity();
    }
}
