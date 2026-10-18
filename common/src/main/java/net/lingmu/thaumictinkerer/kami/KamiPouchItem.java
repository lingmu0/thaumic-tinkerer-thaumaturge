package net.lingmu.thaumictinkerer.kami;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** The original 117 slots are exposed as three vanilla chest pages (54 + 54 + 9). */
public final class KamiPouchItem extends Item implements ICurioItem {
    public KamiPouchItem(Properties properties) { super(properties); }
    @Override public boolean canFitInsideContainerItems() { return false; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int page = Math.floorMod(StackData.integer(stack, "tt_pouch_page"), 3);
            if (player.isShiftKeyDown()) {
                page = (page + 1) % 3; StackData.integer(stack, "tt_pouch_page", page);
                KamiVersion.status(player, Component.translatable("message.thaumictinkerer.pouch_page", page + 1));
            } else open(player, stack, page);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    private static void open(Player player, ItemStack pouch, int page) {
        NonNullList<ItemStack> all = NonNullList.withSize(117, ItemStack.EMPTY);
        pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(all);
        int first = page * 54;
        class StoredContainer extends SimpleContainer {
            boolean loading = true;
            StoredContainer() { super(54); }
            @Override public void setChanged() {
                if (loading) return;
                for (int i = 0; i < 54 && first + i < 117; i++) all.set(first + i, getItem(i).copy());
                pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(all));
                super.setChanged();
            }
            @Override public boolean canPlaceItem(int index, ItemStack stack) { return first + index < 117 && !(stack.getItem() instanceof KamiPouchItem) && stack.getItem().canFitInsideContainerItems(); }
        }
        StoredContainer container = new StoredContainer();
        for (int i = 0; i < 54 && first + i < 117; i++) container.setItem(i, all.get(first + i).copy());
        container.loading = false;
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new ChestMenu(MenuType.GENERIC_9x6, id, inventory, container, 6) {
            {
                for (int i = 0; i < slots.size(); i++) {
                    Slot old = slots.get(i); final int index = i;
                    Slot replacement = new Slot(old.container, old.getContainerSlot(), old.x, old.y) {
                        @Override public boolean mayPickup(Player p) { return getItem() != pouch && super.mayPickup(p); }
                        @Override public boolean mayPlace(ItemStack s) { return index >= 54 || container.canPlaceItem(index, s); }
                    };
                    replacement.index = i; slots.set(i, replacement);
                }
            }
            @Override public boolean stillValid(Player p) { for (int i = 0; i < 36; i++) if (p.getInventory().getItem(i) == pouch) return true; return p.getOffhandItem() == pouch; }
            @Override public ItemStack quickMoveStack(Player p, int slot) {
                if (slot >= 0 && slot < slots.size() && (slots.get(slot).getItem() == pouch || slots.get(slot).getItem().getItem() instanceof KamiPouchItem)) return ItemStack.EMPTY;
                return super.quickMoveStack(p, slot);
            }
        }, Component.translatable("container.thaumictinkerer.ichor_pouch", page + 1)));
    }
}
