package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import net.lingmu.thaumictinkerer.StackData;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public final class BloodSwordItem extends Item {
    public BloodSwordItem(Properties properties) {
        super(properties.sword(TTMaterials.TOOL_THAUMIUM,3,-2.4F).durability(950).attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,10,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-2.4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.MOVEMENT_SPEED,new AttributeModifier(TTContent.id("blood_sword_speed"),0.25,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),EquipmentSlotGroup.MAINHAND).build()));
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player.isShiftKeyDown()) {
            if(!level.isClientSide()){StackData.toggle(stack);net.lingmu.thaumictinkerer.VersionMessages.send(player,Component.translatable(StackData.enabled(stack)?"message.thaumictinkerer.blood_active":"message.thaumictinkerer.blood_inactive"),true);}
        }else player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) {return 72000;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BLOCK;}
}
