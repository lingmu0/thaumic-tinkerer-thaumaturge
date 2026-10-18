package net.lingmu.thaumictinkerer;
import net.minecraft.nbt.CompoundTag;
final class VersionNbt {
    static int integer(CompoundTag tag, String key) { return tag.getInt(key); }
    static String string(CompoundTag tag, String key) { return tag.getString(key); }
}
