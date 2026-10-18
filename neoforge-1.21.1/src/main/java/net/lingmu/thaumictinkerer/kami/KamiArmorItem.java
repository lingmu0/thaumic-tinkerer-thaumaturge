package net.lingmu.thaumictinkerer.kami;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
public final class KamiArmorItem extends ArmorItem implements IVisDiscountGear {
    public final int slot;
    public final boolean awakened;
    public KamiArmorItem(Properties properties, int slot, boolean awakened) {
        super(material(awakened), new Type[]{Type.HELMET, Type.CHESTPLATE, Type.LEGGINGS, Type.BOOTS}[slot], slot == 0 && awakened ? properties.component(DataComponents.UNBREAKABLE, new Unbreakable(true)).component(com.leclowndu93150.thaumaturge.registry.TTDataComponents.GOGGLES_UPGRADE.get(), net.minecraft.util.Unit.INSTANCE) : properties.component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        this.slot = slot; this.awakened = awakened;
    }
    @Override public int getVisDiscount(ItemStack stack) { return slot == 3 ? 3 : 4; }
    @Override public int getEnchantmentValue() { return 20; }
    private static net.minecraft.core.Holder<ArmorMaterial> material(boolean awakened) {
        return net.minecraft.core.Holder.direct(new ArmorMaterial(java.util.Map.of(Type.HELMET, 3, Type.CHESTPLATE, 8, Type.LEGGINGS, 6, Type.BOOTS, 3), 20, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER, () -> net.minecraft.world.item.crafting.Ingredient.EMPTY, java.util.List.of(new ArmorMaterial.Layer(net.lingmu.thaumictinkerer.TTContent.id(awakened ? "ichor_gem" : "ichor"))), 0F, 0F));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (awakened && player.isShiftKeyDown()) {
            StackData.integer(stack, "tt_disabled", StackData.integer(stack, "tt_disabled") == 0 ? 1 : 0);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, hand);
    }
}
