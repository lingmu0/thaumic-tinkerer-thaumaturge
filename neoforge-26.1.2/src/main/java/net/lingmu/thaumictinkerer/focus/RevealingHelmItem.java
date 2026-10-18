package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

public final class RevealingHelmItem extends Item implements IVisDiscountGear {
    public RevealingHelmItem(Properties properties) {super(properties.humanoidArmor(TTMaterials.ARMOR_THAUMIUM,ArmorType.HELMET).durability(500).component(TTDataComponents.GOGGLES_UPGRADE.get(),Unit.INSTANCE));}
    @Override public int getVisDiscount(ItemStack stack) {return 5;}
    @Override public EquipmentSlotGroup getAppliedSlot(ItemStack stack) {return EquipmentSlotGroup.HEAD;}
}
