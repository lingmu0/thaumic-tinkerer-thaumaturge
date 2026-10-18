package net.lingmu.thaumictinkerer.kami;
import java.util.List;
import java.util.Set;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
public final class KamiVersion {
    public static Item.Properties toolProperties(Item.Properties p, KamiToolItem.Kind kind) {
        float baseline = switch(kind) { case SWORD -> 3; case PICK -> 1; case SHOVEL -> 1.5F; case AXE -> 6; };
        var tag = switch(kind) { case PICK -> BlockTags.MINEABLE_WITH_PICKAXE; case SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL; case AXE -> BlockTags.MINEABLE_WITH_AXE; case SWORD -> BlockTags.SWORD_EFFICIENT; };
        return p.component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(tag, 10F)), 1F, 0))
            .attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 5F + baseline, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, kind == KamiToolItem.Kind.SWORD ? -2.4 : -2.8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build());
    }
    public static BlockEntityType<KamiGateBlockEntity> gateEntityType() { return BlockEntityType.Builder.of(KamiGateBlockEntity::new, KamiContent.WARP_GATE.get()).build(null); }
    public static ServerLevel level(ServerPlayer player) { return player.serverLevel(); }
    public static void teleport(ServerPlayer player, ServerLevel target, BlockPos pos) { player.teleportTo(target, pos.getX() + .5, pos.getY() + .1, pos.getZ() + .5, player.getYRot(), player.getXRot()); }
    public static int selected(ServerPlayer player) { return player.getInventory().selected; }
    public static int minY(net.minecraft.world.level.Level level) { return level.getMinBuildHeight(); }
    public static void status(net.minecraft.world.entity.player.Player player, net.minecraft.network.chat.Component component) { if (player instanceof ServerPlayer server) server.sendSystemMessage(component, true); }
    public static void restore(net.neoforged.neoforge.common.util.BlockSnapshot snapshot) { snapshot.restore(3); }
    public static void toolMode(ItemStack stack, int mode) { stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(mode)); }
    public static net.minecraft.world.entity.LivingEntity newTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) { return event.getNewAboutToBeSetTarget(); }
    private KamiVersion() {}
}
