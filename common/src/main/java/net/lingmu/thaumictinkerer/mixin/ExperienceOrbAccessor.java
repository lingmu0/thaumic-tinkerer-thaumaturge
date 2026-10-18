package net.lingmu.thaumictinkerer.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Accessor("count") int tt$getCount();
    @Accessor("count") void tt$setCount(int count);
}
