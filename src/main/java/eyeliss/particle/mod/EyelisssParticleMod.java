package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModCommands;
import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.enchantment.ModEnchantments;
import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.event.OverhealthHandler;
import eyeliss.particle.mod.event.RareItemGlows;
import eyeliss.particle.mod.event.ShadowCurseHandler;
import eyeliss.particle.mod.event.UniqueDrops;
import eyeliss.particle.mod.item.*;
import eyeliss.particle.mod.item.trinkets.ModTrinkets;
import eyeliss.particle.mod.item.trinkets.util.BloodStoneTickHandler;
import eyeliss.particle.mod.network.OverhealthSyncPayload;
import eyeliss.particle.mod.network.RiftGemNetwork;
import eyeliss.particle.mod.network.ShadowBundleScrollPayload; // Added packet import
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.potion.ModPotions;
import eyeliss.particle.mod.recipe.LimitedRecipes;
import eyeliss.particle.mod.recipe.ModRecipes;
import eyeliss.particle.mod.screen.RiftGemScreens;
import eyeliss.particle.mod.sound.ModSounds;
import eyeliss.particle.mod.util.BloodShardDropHandler;
import eyeliss.particle.mod.util.ItemDuplicationChecker;
import eyeliss.particle.mod.util.ModLootTableModifiers;
import eyeliss.particle.mod.util.OverhealthSpawningHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class EyelisssParticleMod implements ModInitializer {
	public static final String MOD_ID = "eyelisspartmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static MinecraftServer CURRENT_SERVER = null;

	@Override
	public void onInitialize() {
		LimitedRecipes.registerLimits();
		ItemDuplicationChecker.register();
		OverhealthHandler.register();
		OverhealthSpawningHandler.register();
		BloodStoneTickHandler.register();
		BloodShardDropHandler.register();
		ModCommands.register();
		RareItemGlows.register();

		RiftGemNetwork.initializePayloads();
		RiftGemNetwork.registerServerReceivers();
		RiftGemScreens.registerScreenHandlers();
		ServerTickEvents.START_SERVER_TICK.register(RiftGemNetwork::tickWarmups);


		ModEffects.register();
		ModPotions.registerPotions();
		ModComponents.registerComponents();
		ModEntities.registerEntities();
		UniqueDrops.registerDrops();
		ModLootTableModifiers.modifyLootTables();
		VanillaItemGroupAdditions.registerItemGroups();
		ModRecipes.registerRecipes();
		ModEnchantments.registerEnchantments();

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
	}
}