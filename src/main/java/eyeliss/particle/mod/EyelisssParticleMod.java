package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModCommands;
import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.block.entity.AdvancedWeaponSmithingBlockEntity;
import eyeliss.particle.mod.block.entity.ModBlockEntities;
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
import eyeliss.particle.mod.network.*;
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.potion.ModPotions;
import eyeliss.particle.mod.recipe.LimitedRecipes;
import eyeliss.particle.mod.recipe.ModRecipes;
import eyeliss.particle.mod.screen.AdvancedWeaponSmithingScreenHandler;
import eyeliss.particle.mod.screen.ModScreenHandlers;
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
		ModRecipes.registerRecipes();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
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
		ModScreenHandlers.registerScreenHandlers();
		ServerTickEvents.START_SERVER_TICK.register(RiftGemNetwork::tickWarmups);

		ModEffects.register();
		ModPotions.registerPotions();
		ModComponents.registerComponents();
		ModEntities.registerEntities();
		UniqueDrops.registerDrops();
		ModLootTableModifiers.modifyLootTables();
		VanillaItemGroupAdditions.registerItemGroups();
		ModEnchantments.registerEnchantments();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModTrinkets.registerModTrinkets();
		ModSpawnEggs.registerModSpawnEggs();
		ModWeapons.registerModWeapons();
		ModParticles.registerParticles();
		ModSounds.registerSounds();
		ShadowCurseHandler.register();

		PayloadTypeRegistry.playS2C().register(OverhealthSyncPayload.ID, OverhealthSyncPayload.CODEC);

		PayloadTypeRegistry.configurationC2S().register(ShadowBundleScrollPayload.ID, ShadowBundleScrollPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ShadowBundleScrollPayload.ID, ShadowBundleScrollPayload.CODEC);

		PayloadTypeRegistry.playC2S().register(SelectWeaponC2SPayload.ID, SelectWeaponC2SPayload.PACKET_CODEC);

		PayloadTypeRegistry.playS2C().register(BlockPosPayload.ID, BlockPosPayload.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SelectWeaponC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				net.minecraft.server.network.ServerPlayerEntity player = context.player();

				if (player.currentScreenHandler instanceof eyeliss.particle.mod.screen.AdvancedWeaponSmithingScreenHandler smithingHandler) {
					if (smithingHandler.getSlot(0).inventory instanceof eyeliss.particle.mod.block.entity.AdvancedWeaponSmithingBlockEntity smithingEntity) {

						smithingEntity.returnMaterialsToPlayer(player);

						var serverAllowedRecipes = smithingEntity.getAllAvailableSmithingRecipes();
						int targetServerIndex = -1;

						for (int i = 0; i < serverAllowedRecipes.size(); i++) {
							if (serverAllowedRecipes.get(i).id().toString().equals(payload.recipeId())) {
								targetServerIndex = i;
								break;
							}
						}

						if (targetServerIndex != -1) {
							smithingHandler.getPropertyDelegate().set(0, targetServerIndex);
							smithingEntity.updateRecipeOutput();
						} else {
							smithingHandler.getPropertyDelegate().set(0, 0);
							smithingEntity.updateRecipeOutput();
						}
					}

					smithingHandler.sendContentUpdates();
					player.getInventory().markDirty();
					smithingHandler.onContentChanged(smithingHandler.getSlot(0).inventory);
				}
			});
		});

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