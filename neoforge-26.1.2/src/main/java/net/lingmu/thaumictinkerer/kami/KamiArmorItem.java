package net.lingmu.thaumictinkerer.kami;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.util.Unit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
public final class KamiArmorItem extends Item implements IVisDiscountGear {
    public final int slot;
    public final boolean awakened;
    public KamiArmorItem(Properties properties, int slot, boolean awakened) {
        super(armor(properties, slot, awakened));
        this.slot = slot; this.awakened = awakened;
    }
    @Override public int getVisDiscount(ItemStack stack) { return slot == 3 ? 3 : 4; }
    private static Properties armor(Properties properties, int slot, boolean awakened) {
        ArmorMaterial material = new ArmorMaterial(1, java.util.Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3), 20, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, net.minecraft.tags.ItemTags.NETHERITE_TOOL_MATERIALS, net.minecraft.resources.ResourceKey.create(EquipmentAssets.ROOT_ID, net.lingmu.thaumictinkerer.TTContent.id(awakened ? "ichor_gem" : "ichor")));
        properties.humanoidArmor(material, new ArmorType[]{ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS}[slot]).component(DataComponents.UNBREAKABLE, Unit.INSTANCE).enchantable(20);
        if (slot == 0 && awakened) properties.component(com.leclowndu93150.thaumaturge.registry.TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE);
        return properties;
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (awakened && player.isShiftKeyDown()) { StackData.integer(stack, "tt_disabled", StackData.integer(stack, "tt_disabled") == 0 ? 1 : 0); return InteractionResult.SUCCESS; }
        return super.use(level, player, hand);
    }
}
