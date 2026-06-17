package eyeliss.particle.mod.item.trinkets;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModTrinkets {
    public static final Item MIDAS_GOLD = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "midas_gold"),
            new MidasGoldItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item SADIMS_IRON = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "sadims_iron"),
            new SadimsIronItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static void registerModTrinkets() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Trinket Items for " + EyelisssParticleMod.MOD_ID);
    }
}