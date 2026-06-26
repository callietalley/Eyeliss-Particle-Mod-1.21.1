package eyeliss.particle.mod.registry;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModItemTags {
    public static final TagKey<Item> SWORD_ENGRAVING_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "sword_engraving_pool"));
    public static final TagKey<Item> TOOL_ENGRAVING_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "tool_engraving_pool"));
    public static final TagKey<Item> ARMOR_ENGRAVING_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "armor_engraving_pool"));
    public static final TagKey<Item> GENERAL_ENGRAVING_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "general_engraving_pool"));
    public static final TagKey<Item> WEAPON_ENGRAVING_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "weapon_engraving_pool"));
}
