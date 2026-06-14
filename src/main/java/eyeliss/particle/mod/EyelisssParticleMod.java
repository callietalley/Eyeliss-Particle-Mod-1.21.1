package eyeliss.particle.mod;

import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.enchantment.ModEnchantments;
import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.event.OverhealthHandler;
import eyeliss.particle.mod.event.ShadowCurseHandler;
import eyeliss.particle.mod.item.*;
import eyeliss.particle.mod.item.trinkets.ModTrinkets;
import eyeliss.particle.mod.network.OverhealthSyncPayload;
import eyeliss.particle.mod.network.ShadowBundleScrollPayload; // Added packet import
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.potion.ModPotions;
import eyeliss.particle.mod.recipe.ModRecipes;
import eyeliss.particle.mod.sound.ModSounds;
import eyeliss.particle.mod.util.ModLootTableModifiers;
import eyeliss.particle.mod.util.OverhealthSpawningHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry; // Added networking imports
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class EyelisssParticleMod implements ModInitializer {
	public static final String MOD_ID = "eyelisspartmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String TEAM_NAME = "purple_glow_team";

	private static final TagKey<Item> PURPLE_GLOW_TAG = TagKey.of(
			RegistryKeys.ITEM,
			Identifier.of(MOD_ID, "glows_purple")
	);

	@Override
	public void onInitialize() {
		ModEffects.register();
		ModPotions.registerPotions();
		ModComponents.registerComponents();
		ModEntities.registerEntities();
		ModLootTableModifiers.modifyLootTables();
		VanillaItemGroupAdditions.registerItemGroups();
		ModRecipes.registerRecipes();
		ModEnchantments.registerEnchantments();


		OverhealthHandler.register();
		OverhealthSpawningHandler.register();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModTrinkets.registerModTrinkets();
		ModSpawnEggs.registerModSpawnEggs();
		ModWeapons.registerModWeapons();
		ModBlocks.registerModBlocks();
		ModParticles.registerParticles();
		ModSounds.registerSounds();
		ShadowCurseHandler.register();

		PayloadTypeRegistry.playS2C().register(OverhealthSyncPayload.ID, OverhealthSyncPayload.CODEC);

		PayloadTypeRegistry.configurationC2S().register(ShadowBundleScrollPayload.ID, ShadowBundleScrollPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ShadowBundleScrollPayload.ID, ShadowBundleScrollPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ShadowBundleScrollPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				var player = context.player();
				if (player.currentScreenHandler != null) {
					int targetSlotId = payload.slotId();

					if (targetSlotId >= 0 && targetSlotId < player.currentScreenHandler.slots.size()) {
						Slot serverSlot = player.currentScreenHandler.getSlot(targetSlotId);

						if (serverSlot.hasStack() && serverSlot.getStack().isOf(ModItems.SHADOW_BUNDLE)) {
							ItemStack bundleStack = serverSlot.getStack();
							BundleContentsComponent contents = bundleStack.get(DataComponentTypes.BUNDLE_CONTENTS);

							if (contents != null && !contents.isEmpty()) {
								List<ItemStack> itemArrayList = new ArrayList<>(contents.stream().toList());

								if (itemArrayList.size() > 1) {
									if (payload.scrollUp()) {
										ItemStack firstItem = itemArrayList.remove(0);
										itemArrayList.add(firstItem);
									} else {
										ItemStack lastItem = itemArrayList.remove(itemArrayList.size() - 1);
										itemArrayList.add(0, lastItem);
									}
									bundleStack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(itemArrayList));
								}
							}
						}
					}
				}
			});
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();
			Team purpleTeam = scoreboard.getTeam(TEAM_NAME);

			if (purpleTeam == null) {
				purpleTeam = scoreboard.addTeam(TEAM_NAME);
				purpleTeam.setColor(Formatting.LIGHT_PURPLE);
				purpleTeam.setDisplayName(Text.literal("Purple Glow Team"));
			}

			for (var world : server.getWorlds()) {
				for (ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, itemEntity -> true)) {

					ItemStack stack = itemEntity.getStack();

					if (stack.isIn(PURPLE_GLOW_TAG) || Boolean.TRUE.equals(stack.get(ModComponents.IS_CURSED))) {
						String scoreHolderName = itemEntity.getNameForScoreboard();

						if (!purpleTeam.getPlayerList().contains(scoreHolderName)) {
							scoreboard.addScoreHolderToTeam(scoreHolderName, purpleTeam);
						}

						itemEntity.setGlowing(true);
					}
				}
			}
		});
	}
}