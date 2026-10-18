package net.lingmu.thaumictinkerer.kami;

import java.util.List;
import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class KamiUtilityItem extends Item implements ICurioItem {
    public enum Kind { CAT_AMULET, BLOCK_TALISMAN, PLACEMENT_MIRROR, PROTOCLAY }
    public final Kind kind;
    public KamiUtilityItem(Properties properties, Kind kind) { super(properties); this.kind = kind; }
    public static Block storedBlock(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(StackData.string(stack, "tt_stored_block"));
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
    }
    public static int count(ItemStack stack) { return StackData.integer(stack, "tt_block_count"); }
    public static int remove(ItemStack stack, int amount) {
        int taken = Math.min(amount, count(stack));
        StackData.integer(stack, "tt_block_count", count(stack) - taken);
        return taken;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || (kind != Kind.BLOCK_TALISMAN && kind != Kind.PLACEMENT_MIRROR)) return InteractionResult.PASS;
        Level level = context.getLevel(); ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            if (kind == Kind.PLACEMENT_MIRROR || count(stack) == 0) {
                Block block = level.getBlockState(context.getClickedPos()).getBlock();
                if (block.asItem() != Items.AIR && level.getBlockEntity(context.getClickedPos()) == null) StackData.string(stack, "tt_stored_block", BuiltInRegistries.BLOCK.getKey(block).toString());
            }
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer server)) return InteractionResult.SUCCESS;
        Block block = storedBlock(stack); if (block == Blocks.AIR) return InteractionResult.PASS;
        if (kind == Kind.BLOCK_TALISMAN) {
            if (count(stack) == 0 && !player.isCreative()) return InteractionResult.FAIL;
            if (place(server, context.getClickedPos().relative(context.getClickedFace()), block) && !player.isCreative()) remove(stack, 1);
        } else {
            int size = Math.max(3, StackData.integer(stack, "tt_mirror_size")); int range = size / 2;
            Direction side = context.getClickedFace(); BlockPos origin = context.getClickedPos().relative(side);
            for (int a = -range; a <= range; a++) for (int b = -range; b <= range; b++) {
                BlockPos pos = switch (side.getAxis()) { case X -> origin.offset(0, a, b); case Y -> origin.offset(a, 0, b); case Z -> origin.offset(a, b, 0); };
                if (!hasBlock(player, block)) return InteractionResult.SUCCESS;
                if (place(server, pos, block) && !player.isCreative()) consumeBlock(player, block);
            }
        }
        return InteractionResult.SUCCESS;
    }
    static boolean place(ServerPlayer player, BlockPos pos, Block block) {
        Level level = player.level();
        if (!level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, player.getMainHandItem()) || !level.getBlockState(pos).canBeReplaced() || !block.defaultBlockState().canSurvive(level, pos) || !level.isUnobstructed(block.defaultBlockState(), pos, net.minecraft.world.phys.shapes.CollisionContext.of(player))) return false;
        var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, pos);
        if (!level.setBlock(pos, block.defaultBlockState(), 3)) return false;
        if (net.neoforged.neoforge.event.EventHooks.onBlockPlace(player, snapshot, Direction.UP)) { KamiVersion.restore(snapshot); return false; }
        return true;
    }
    static boolean hasBlock(Player player, Block block) {
        if (player.isCreative()) return true;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, new ItemStack(block)) || stack.is(KamiContent.BLOCK_TALISMAN.get()) && storedBlock(stack) == block && count(stack) > 0) return true;
        }
        return false;
    }
    static void consumeBlock(Player player, Block block) {
        for (int i = 0; i < 36; i++) { ItemStack stack = player.getInventory().getItem(i); if (ItemStack.isSameItemSameComponents(stack, new ItemStack(block))) { stack.shrink(1); return; } }
        for (int i = 0; i < 36; i++) { ItemStack stack = player.getInventory().getItem(i); if (stack.is(KamiContent.BLOCK_TALISMAN.get()) && storedBlock(stack) == block && count(stack) > 0) { remove(stack, 1); return; } }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (kind == Kind.PLACEMENT_MIRROR && player.isShiftKeyDown()) {
                int size = Math.max(3, StackData.integer(stack, "tt_mirror_size"));
                size = size >= 11 ? 3 : size + 2; StackData.integer(stack, "tt_mirror_size", size);
                KamiVersion.status(player, Component.literal(size + " x " + size));
            } else if (kind == Kind.BLOCK_TALISMAN && player.isShiftKeyDown()) {
                int removed = remove(stack, 64);
                if (removed > 0) { ItemStack output = new ItemStack(storedBlock(stack), removed); if (!player.getInventory().add(output)) player.drop(output, false); }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public void curioTick(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof ServerPlayer player)) return;
        if (kind == Kind.CAT_AMULET) KamiEvents.repelMobs(player);
        if (kind == Kind.BLOCK_TALISMAN) absorbBlocks(player, stack);
    }
    static void absorbBlocks(Player player, ItemStack talisman) {
        Block block = storedBlock(talisman); if (block == Blocks.AIR) return;
        ItemStack template = new ItemStack(block);
        int keep = -1;
        for (int i = 0; i < 36; i++) if (ItemStack.isSameItemSameComponents(player.getInventory().getItem(i), template)) { keep = i; break; }
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (i != keep && ItemStack.isSameItemSameComponents(stack, template)) {
                int room = Integer.MAX_VALUE - count(talisman); int taken = Math.min(room, stack.getCount());
                StackData.integer(talisman, "tt_block_count", count(talisman) + taken); stack.shrink(taken);
            }
        }
        if (keep >= 0) {
            ItemStack retained = player.getInventory().getItem(keep);
            retained.grow(remove(talisman, Math.max(0, retained.getMaxStackSize() - retained.getCount())));
        } else if (count(talisman) > 0) {
            int taken = Math.min(count(talisman), template.getMaxStackSize());
            ItemStack output = new ItemStack(block, taken);
            player.getInventory().add(output);
            remove(talisman, taken - output.getCount());
        }
    }
    public void addTooltip(ItemStack stack, List<Component> tooltip) {
        if (kind == Kind.BLOCK_TALISMAN || kind == Kind.PLACEMENT_MIRROR) tooltip.add(Component.literal(StackData.string(stack, "tt_stored_block") + (kind == Kind.BLOCK_TALISMAN ? " x " + count(stack) : "")));
    }
}
