package eyeliss.particle.mod.component;

import eyeliss.particle.mod.EyelisssParticleMod;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModComponents {

    public static final ComponentType<Boolean> IS_CURSED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyelisspartmod", "is_cursed"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    public static final ComponentType<List<ItemStack>> SHADOW_BUNDLE_CONTENTS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyelisspartmod", "shadow_bundle_contents"),
            ComponentType.<List<ItemStack>>builder()
                    .codec(ItemStack.CODEC.listOf())
                    .build()
    );

    public static final ComponentType<SyringeContents> SYRINGE_CONTENTS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EyelisssParticleMod.MOD_ID, "syringe_contents"),
            ComponentType.<SyringeContents>builder().codec(SyringeContents.CODEC).build()
    );

    public static final ComponentType<List<EngravingContents>> ENGRAVING_CONTENTS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EyelisssParticleMod.MOD_ID, "engraving_contents"),
            ComponentType.<List<EngravingContents>>builder().codec(EngravingContents.CODEC.listOf()).build()
    );

    public static final ComponentType<Boolean> DWARVEN_TOUCH = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyelisspartmod", "dwarven_touch"),
            ComponentType.<Boolean>builder().codec(com.mojang.serialization.Codec.BOOL).build()
    );

    public static final ComponentType<BloodShrivingCharge> SHRIVING_CHARGE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyelisspartmod", "shriving_charge"),
            ComponentType.<BloodShrivingCharge>builder().codec(BloodShrivingCharge.CODEC).build()
    );

    public static final ComponentType<BlockShrivingCharge> BLOCK_CHARGE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyelisspartmod", "block_charge"),
            ComponentType.<BlockShrivingCharge>builder().codec(BlockShrivingCharge.CODEC).build()
    );

    public static final net.minecraft.component.ComponentType<BlessedCharge> BLESSED_CHARGE = net.minecraft.registry.Registry.register(
            net.minecraft.registry.Registries.DATA_COMPONENT_TYPE,
            net.minecraft.util.Identifier.of("eyelisspartmod", "blessed_charge"),
            net.minecraft.component.ComponentType.<BlessedCharge>builder().codec(BlessedCharge.CODEC).build()
    );

    public static void registerComponents() {
        System.out.println("Initializing Data Components for " + EyelisssParticleMod.MOD_ID);

        TrackingID.load();
        ModEngravings.registerPools();
    }
}
