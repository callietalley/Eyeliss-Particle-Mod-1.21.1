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

    public static void registerComponents() {
        System.out.println("Initializing Data Components for " + EyelisssParticleMod.MOD_ID);

        TrackingID.load();
    }
}
