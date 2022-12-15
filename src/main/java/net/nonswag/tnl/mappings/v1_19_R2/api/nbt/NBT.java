package net.nonswag.tnl.mappings.v1_19_R3.api.nbt;

import net.minecraft.nbt.CompoundTag;
import net.nonswag.tnl.listener.api.nbt.NBTTag;

import javax.annotation.Nonnull;

public class NBT extends NBTTag {

    @Nonnull
    private final CompoundTag compound;

    public NBT(@Nonnull CompoundTag compound) {
        this.compound = compound;
    }

    @Nonnull
    @Override
    public String get(@Nonnull String s) {
        return versioned().getString(s);
    }

    @Override
    public int getInt(@Nonnull String s) {
        return versioned().getInt(s);
    }

    @Override
    public double getDouble(@Nonnull String s) {
        return versioned().getDouble(s);
    }

    @Override
    public float getFloat(@Nonnull String s) {
        return versioned().getFloat(s);
    }

    @Override
    public long getLong(@Nonnull String s) {
        return versioned().getLong(s);
    }

    @Override
    public short getShort(@Nonnull String s) {
        return versioned().getShort(s);
    }

    @Override
    public byte getByte(@Nonnull String s) {
        return versioned().getByte(s);
    }

    @Override
    public boolean getBoolean(@Nonnull String s) {
        return versioned().getBoolean(s);
    }

    @Override
    public int[] getIntArray(@Nonnull String s) {
        return versioned().getIntArray(s);
    }

    @Override
    public byte[] getByteArray(@Nonnull String s) {
        return versioned().getByteArray(s);
    }

    @Override
    public void set(@Nonnull String s, @Nonnull String s1) {
        versioned().putString(s, s1);
    }

    @Override
    public void set(@Nonnull String s, int i) {
        versioned().putInt(s, i);
    }

    @Override
    public void set(@Nonnull String s, double v) {
        versioned().putDouble(s, v);
    }

    @Override
    public void set(@Nonnull String s, float v) {
        versioned().putFloat(s, v);
    }

    @Override
    public void set(@Nonnull String s, long l) {
        versioned().putLong(s, l);
    }

    @Override
    public void set(@Nonnull String s, short i) {
        versioned().putShort(s, i);
    }

    @Override
    public void set(@Nonnull String s, byte b) {
        versioned().putByte(s, b);
    }

    @Override
    public void set(@Nonnull String s, boolean b) {
        versioned().putBoolean(s, b);
    }

    @Override
    public void set(@Nonnull String s, int[] ints) {
        versioned().putIntArray(s, ints);
    }

    @Override
    public void set(@Nonnull String s, byte[] bytes) {
        versioned().putByteArray(s, bytes);
    }

    @Nonnull
    @Override
    public NBT append(@Nonnull NBTTag tag) {
        if (tag instanceof NBT nbt) versioned().merge(nbt.versioned());
        return this;
    }

    @Nonnull
    @Override
    public CompoundTag versioned() {
        return compound;
    }
}
