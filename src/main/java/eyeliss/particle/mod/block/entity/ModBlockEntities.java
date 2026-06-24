package eyeliss.particle.mod.block.entity;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<AdvancedWeaponSmithingBlockEntity> ADVANCED_WEAPON_SMITHING_ENTITY;

    public static void registerBlockEntities() {
        ADVANCED_WEAPON_SMITHING_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(EyelisssParticleMod.MOD_ID, "advanced_weapon_smithing_entity"),
                BlockEntityType.Builder.create(AdvancedWeaponSmithingBlockEntity::new,
                        ModBlocks.ADVANCED_WEAPON_SMITHING_BLOCK).build(null)
        );
    }
}
