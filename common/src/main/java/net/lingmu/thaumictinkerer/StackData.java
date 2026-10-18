package net.lingmu.thaumictinkerer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class StackData {
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
    public static int integer(ItemStack stack, String key) { return VersionNbt.integer(read(stack), key); }
    public static boolean enabled(ItemStack stack) { return integer(stack, "tt_enabled") != 0; }
    public static void integer(ItemStack stack, String key, int value) {
        CompoundTag tag = read(stack);
        tag.putInt(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    public static String string(ItemStack stack, String key) { return VersionNbt.string(read(stack), key); }
    public static void string(ItemStack stack, String key, String value) {
        CompoundTag tag=read(stack); tag.putString(key,value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    public static void toggle(ItemStack stack) { integer(stack, "tt_enabled", enabled(stack) ? 0 : 1); }
    private StackData() {}
}
