package eyeliss.particle.mod.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    // This safely targets the protected "jumping" boolean field in Minecraft's engine code
    @Accessor("jumping")
    boolean isJumping();
}