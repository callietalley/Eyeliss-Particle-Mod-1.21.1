package eyeliss.particle.mod.entity;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<UmberwitherEntity> UMBERWITHER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(EyelisssParticleMod.MOD_ID, "umberwither"),
            EntityType.Builder.create(UmberwitherEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.9F, 3.5F)
                    .makeFireImmune()
                    .build()
    );

    public static final EntityType<ThrownSyringeEntity> THROWN_SYRINGE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(EyelisssParticleMod.MOD_ID, "thrown_syringe"),
            EntityType.Builder.<ThrownSyringeEntity>create(ThrownSyringeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5F, 0.5F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(20)
                    .build()
    );

    public static void registerEntities() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Entities for " + EyelisssParticleMod.MOD_ID);

        FabricDefaultAttributeRegistry.register(UMBERWITHER, UmberwitherEntity.createUmberwitherAttributes());
    }
}