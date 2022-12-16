package net.nonswag.tnl.mappings.v1_19_R2.api.nbt;

import net.minecraft.nbt.CompoundTag;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.tnl.listener.api.nbt.NBTTag;

import javax.annotation.ParametersAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NBT extends NBTTag {

    private final CompoundTag compound;

    public NBT(CompoundTag compound) {
        this.compound = compound;
    }

    @Override
    public String get(String s) {
        return versioned().getString(s);
    }

    @Override
    public int getInt(String s) {
        return versioned().getInt(s);
    }

    @Override
    public double getDouble(String s) {
        return versioned().getDouble(s);
    }

    @Override
    public float getFloat(String s) {
        return versioned().getFloat(s);
    }

    @Override
    public long getLong(String s) {
        return versioned().getLong(s);
    }

    @Override
    public short getShort(String s) {
        return versioned().getShort(s);
    }

    @Override
    public byte getByte(String s) {
        return versioned().getByte(s);
    }

    @Override
    public boolean getBoolean(String s) {
        return versioned().getBoolean(s);
    }

    @Override
    public int[] getIntArray(String s) {
        return versioned().getIntArray(s);
    }

    @Override
    public byte[] getByteArray(String s) {
        return versioned().getByteArray(s);
    }

    @Override
    public void set(String s, String s1) {
        versioned().putString(s, s1);
    }

    @Override
    public void set(String s, int i) {
        versioned().putInt(s, i);
    }

    @Override
    public void set(String s, double v) {
        versioned().putDouble(s, v);
    }

    @Override
    public void set(String s, float v) {
        versioned().putFloat(s, v);
    }

    @Override
    public void set(String s, long l) {
        versioned().putLong(s, l);
    }

    @Override
    public void set(String s, short i) {
        versioned().putShort(s, i);
    }

    @Override
    public void set(String s, byte b) {
        versioned().putByte(s, b);
    }

    @Override
    public void set(String s, boolean b) {
        versioned().putBoolean(s, b);
    }

    @Override
    public void set(String s, int[] ints) {
        versioned().putIntArray(s, ints);
    }

    @Override
    public void set(String s, byte[] bytes) {
        versioned().putByteArray(s, bytes);
    }

    @Override
    public NBT append(NBTTag tag) {
        if (tag instanceof NBT nbt) versioned().merge(nbt.versioned());
        return this;
    }

    @Override
    public CompoundTag versioned() {
        return compound;
    }
}
