package net.lingmu.thaumictinkerer;
import net.minecraft.nbt.CompoundTag;
final class VersionNbt {
    static int integer(CompoundTag tag, String key) { return tag.getIntOr(key, 0); }
    static String string(CompoundTag tag, String key) { return tag.getStringOr(key, ""); }
}
