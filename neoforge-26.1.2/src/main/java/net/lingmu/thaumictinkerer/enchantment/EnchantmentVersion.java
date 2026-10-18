package net.lingmu.thaumictinkerer.enchantment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
final class EnchantmentVersion {
    static void slowFall(LivingEntity entity,int level) {
        if (entity.getDeltaMovement().y < -0.0784000015258789 && entity.fallDistance>=2.9) {
            var motion=entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x,motion.y/(1+level*0.33F),motion.z);
            entity.fallDistance=Math.max(2.9,entity.fallDistance-level/3F);
        }
    }
    static void shockwave(LivingEntity entity,float amount) {
        if(entity.level() instanceof ServerLevel server) entity.hurtServer(server,entity.damageSources().fall(),amount);
    }
    static ItemStack smelt(ServerLevel level,ItemStack input) {
        var single=new SingleRecipeInput(input);
        var recipe=level.recipeAccess().getRecipeFor(RecipeType.SMELTING,single,level);
        if(recipe.isEmpty()) return ItemStack.EMPTY;
        ItemStack result=recipe.get().value().assemble(single);
        return result;
    }
}
