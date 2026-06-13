package eyeliss.particle.mod.entity;

import eyeliss.particle.mod.item.ModWeapons;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class ThrownSyringeEntity extends PersistentProjectileEntity {

    public ThrownSyringeEntity(EntityType<? extends ThrownSyringeEntity> type, World world) {
        super(type, world);
        this.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
    }

    public ThrownSyringeEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_SYRINGE, owner, world, stack, null);
        this.setStack(stack.copy());
        this.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY; // <-- ADD HERE TOO
    }

    public ItemStack getSyringeStack() {
        return this.getItemStack();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.getOwner() instanceof LivingEntity owner) {

            Box searchBox = this.getBoundingBox().expand(8.0);
            List<LivingEntity> nearbyTargets = this.getWorld().getEntitiesByClass(LivingEntity.class, searchBox,
                    target -> target.isAlive() && target != owner && !target.isSpectator());

            LivingEntity target = null;
            double closestDistance = Double.MAX_VALUE;
            for (LivingEntity potentialTarget : nearbyTargets) {
                double distance = this.squaredDistanceTo(potentialTarget);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    target = potentialTarget;
                }
            }

            if (target != null) {
                Vec3d targetDir = target.getEyePos().subtract(this.getPos()).normalize();

                double currentSpeed = this.getVelocity().length();

                this.setVelocity(targetDir.multiply(currentSpeed));
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient()) return;

        if (entityHitResult.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity attacker) {
            DamageSource source = this.getDamageSources().thrown(this, this.getOwner());
            ItemStack stack = this.getSyringeStack();

            java.util.Optional<net.minecraft.registry.entry.RegistryEntry.Reference<net.minecraft.enchantment.Enchantment>> infusionEntry =
                    this.getWorld().getRegistryManager()
                            .getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                            .getOptional(eyeliss.particle.mod.enchantment.ModEnchantments.CHEMICAL_INFUSION);

            boolean hasInfusion = infusionEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(infusionEntry.get(), stack) > 0;
            float finalDamage = hasInfusion ? 2.0F : 8.0F;

            if (target.damage(source, finalDamage)) {
                // Trigger potion injection mechanics
                if (stack.getItem() instanceof eyeliss.particle.mod.item.specialweapons.SyringeItem syringeItem) {
                    syringeItem.postHit(stack, target, attacker);
                }

                if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    this.getWorld().playSound(
                            null,
                            target.getX(), target.getY(), target.getZ(),
                            net.minecraft.sound.SoundEvent.of(Identifier.of("spell_engine", "generic_poison_impact")),
                            net.minecraft.sound.SoundCategory.PLAYERS,
                            0.1F, // 🔊 Same 0.1 Volume
                            0.9F + this.random.nextFloat() * 0.2F // Dynamic pitch variety
                    );
                }

                this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
            }
        }
        this.discard();
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModWeapons.SYRINGE);
    }
}
