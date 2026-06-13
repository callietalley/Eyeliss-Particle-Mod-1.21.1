package eyeliss.particle.mod.client;

import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.SyringeContents;
import eyeliss.particle.mod.item.ModWeapons;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance; // Added missing import
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SyringeColor {

    public static void registerColor() {
        // Registers the multi-potion blending fluid color handler
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                SyringeContents contents = stack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

                if (contents == null || contents.payloads().isEmpty() || contents.payloads().getFirst().effectId().equals("minecraft:empty")) {
                    return 0xFFFFFF;
                }

                List<StatusEffectInstance> allCombinedEffects = new ArrayList<>();

                for (SyringeContents.Payload payload : contents.payloads()) {
                    Optional<Potion> potionOptional = Registries.POTION.getOrEmpty(Identifier.of(payload.effectId()));
                    potionOptional.ifPresent(potion -> allCombinedEffects.addAll(potion.getEffects()));
                }

                if (!allCombinedEffects.isEmpty()) {
                    return PotionContentsComponent.getColor(allCombinedEffects);
                }
            }
            return -1;
        }, ModWeapons.SYRINGE);
    }
}
