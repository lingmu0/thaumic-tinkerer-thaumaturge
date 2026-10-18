package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class RevealingHelmItem extends ArmorItem implements IVisDiscountGear {
    public RevealingHelmItem(Properties properties) {super(TTMaterials.ARMOR_THAUMIUM,Type.HELMET,properties.component(TTDataComponents.GOGGLES_UPGRADE.get(),Unit.INSTANCE));}
    @Override public int getVisDiscount(ItemStack stack) {return 5;}
}
