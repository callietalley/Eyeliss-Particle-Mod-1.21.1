package eyeliss.particle.mod.enchantment;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static final RegistryKey<Enchantment> CHEMICAL_INFUSION = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(EyelisssParticleMod.MOD_ID, "syringe/chemical_infusion")
    );

    public static final RegistryKey<Enchantment> CHEMICAL_BURST = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(EyelisssParticleMod.MOD_ID, "syringe/chemical_burst")
    );

    public static final RegistryKey<Enchantment> RANGED_DELIVERY = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(EyelisssParticleMod.MOD_ID, "syringe/ranged_delivery")
    );

    public static void registerEnchantments() {
        System.out.println("Initializing Custom Enchantments for " + EyelisssParticleMod.MOD_ID);
    }
}