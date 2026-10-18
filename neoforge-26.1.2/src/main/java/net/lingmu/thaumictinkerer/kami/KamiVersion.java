package net.lingmu.thaumictinkerer.kami;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntityType;
public final class KamiVersion {
    private static final ToolMaterial MATERIAL = new ToolMaterial(net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 10F, 5F, 25, net.minecraft.tags.ItemTags.NETHERITE_TOOL_MATERIALS);
    public static Item.Properties toolProperties(Item.Properties p, KamiToolItem.Kind kind) {
        p = switch(kind) { case PICK -> p.pickaxe(MATERIAL, 1F, -2.8F); case SHOVEL -> p.shovel(MATERIAL, 1.5F, -2.8F); case AXE -> p.axe(MATERIAL, 6F, -2.8F); case SWORD -> p.sword(MATERIAL, 3F, -2.4F); };
        return p.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
    }
    public static BlockEntityType<KamiGateBlockEntity> gateEntityType() { return new BlockEntityType<>(KamiGateBlockEntity::new, KamiContent.WARP_GATE.get()); }
    public static ServerLevel level(ServerPlayer player) { return player.level(); }
    public static void teleport(ServerPlayer player, ServerLevel target, BlockPos pos) { player.teleportTo(target, pos.getX() + .5, pos.getY() + .1, pos.getZ() + .5, Set.of(), player.getYRot(), player.getXRot(), true); }
    public static int selected(ServerPlayer player) { return player.getInventory().getSelectedSlot(); }
    public static int minY(net.minecraft.world.level.Level level) { return level.getMinY(); }
    public static void status(net.minecraft.world.entity.player.Player player, net.minecraft.network.chat.Component component) { if (player instanceof ServerPlayer server) server.sendSystemMessage(component, true); }
    public static void restore(net.neoforged.neoforge.common.util.BlockSnapshot snapshot) { snapshot.restore(3); }
    public static void toolMode(ItemStack stack, int mode) { stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(java.util.List.of((float) mode), java.util.List.of(), java.util.List.of(), java.util.List.of())); }
    public static net.minecraft.world.entity.LivingEntity newTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) { return event.getNewAboutToBeSetTarget(); }
    private KamiVersion() {}
}
