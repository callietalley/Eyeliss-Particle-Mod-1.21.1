package eyeliss.particle.mod.network;

import eyeliss.particle.mod.api.IActiveTrinketItem;
import eyeliss.particle.mod.item.trinkets.ModTrinkets;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketComponent;
import eyeliss.particle.mod.screen.RiftGemScreenHandler;
import eyeliss.particle.mod.screen.RiftGemBindScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RiftGemNetwork {

    private static final Map<UUID, Integer> WARMUP_PLAYERS = new HashMap<>();
    private static final Map<UUID, WarpTarget> WARMUP_TARGETS = new HashMap<>();
    private static final Set<String> BIOMES = Set.of("forest", "desert", "tundra", "deepforest", "shore");

    private record WarpTarget(double x, double y, double z, String dimensionStr, ItemStack gemStack, long currentWorldTime) {}

    public static void initializePayloads() {
        PayloadTypeRegistry.playC2S().register(RiftGemPayloads.OpenRiftScreenPayload.ID, RiftGemPayloads.OpenRiftScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RiftGemPayloads.BindEnvironmentPayload.ID, RiftGemPayloads.BindEnvironmentPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RiftGemPayloads.TypeWarpPayload.ID, RiftGemPayloads.TypeWarpPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(RiftGemPayloads.OpenRiftScreenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                java.util.Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
                if (component.isEmpty()) return;

                // Loop through all equipped items to find one that supports active inputs
                for (var equip : component.get().getAllEquipped()) {
                    ItemStack stack = equip.getRight();
                    if (stack.getItem() instanceof IActiveTrinketItem activeTrinket) {
                        // Dynamically fire the trinket's unique active feature
                        activeTrinket.onTrinketKeybindPressed(player, stack, payload.isSneaking());
                        return; // Stop searching once we process the first valid active trinket
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RiftGemPayloads.BindEnvironmentPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                java.util.Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(player);
                if (compOpt.isEmpty()) return;

                for (var equip : compOpt.get().getAllEquipped()) {
                    ItemStack slotStack = equip.getRight();
                    if (slotStack.isOf(ModTrinkets.RIFT_GEM)) {

                        String env = payload.envName().toLowerCase();
                        BlockPos pos = player.getBlockPos();
                        String dim = player.getWorld().getRegistryKey().getValue().toString();

                        NbtComponent nbtComponent = slotStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                        NbtCompound nbt = nbtComponent.copyNbt();
                        String playerUUID = player.getUuidAsString();

                        nbt.putDouble(playerUUID + "_" + env + "X", pos.getX() + 0.5);
                        nbt.putDouble(playerUUID + "_" + env + "Y", pos.getY() + 0.1);
                        nbt.putDouble(playerUUID + "_" + env + "Z", pos.getZ() + 0.5);
                        nbt.putString(playerUUID + "_" + env + "Dim", dim);
                        nbt.putBoolean(playerUUID + "_" + env + "Bound", true);

                        slotStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        String displayEnv = env.substring(0, 1).toUpperCase() + env.substring(1);
                        player.sendMessage(Text.literal("§d[Rift] Successfully bound coordinates to frequency: " + displayEnv), true);

                        ServerWorld world = player.getServerWorld();

                        net.minecraft.util.Identifier soundId = net.minecraft.util.Identifier.of("spell_engine", "bind_spell");
                        net.minecraft.sound.SoundEvent bindSound = net.minecraft.sound.SoundEvent.of(soundId);

                        float randomPitch = 1.0f + (player.getRandom().nextFloat() - 0.5f) * 0.3f;
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), bindSound, SoundCategory.PLAYERS, 1.0f, randomPitch);

                        org.joml.Vector3f purpleColor = new org.joml.Vector3f(0.58f, 0.0f, 0.86f);
                        net.minecraft.particle.DustParticleEffect purpleDust = new net.minecraft.particle.DustParticleEffect(purpleColor, 1.2f);

                        double chestY = player.getY() + 1.1;

                        world.spawnParticles(
                                purpleDust,
                                player.getX(), chestY, player.getZ(),
                                25,
                                0.2, 0.2, 0.2,
                                0.15
                        );

                        return;
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RiftGemPayloads.TypeWarpPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                String env = payload.typedName().toLowerCase();

                if ("INVALID_RIFT_FREQUENCY_PENALTY".equals(env)) {
                    player.damage(player.getWorld().getDamageSources().magic(), 4.0f);
                    player.sendMessage(Text.literal("§cRift Instability backfire! Incorrect frequency."), true);
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 0.5f, 1.8f);
                    return;
                }

                java.util.Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(player);
                if (compOpt.isEmpty()) return;

                for (var equip : compOpt.get().getAllEquipped()) {
                    ItemStack slotStack = equip.getRight();
                    if (slotStack.isOf(ModTrinkets.RIFT_GEM)) {

                        NbtComponent nbtComponent = slotStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                        NbtCompound nbt = nbtComponent.copyNbt();

                        long time = player.getWorld().getTime();
                        long lastUsed = nbt.getLong("LastWarpTime");

                        if (time - lastUsed < 160) {
                            player.sendMessage(Text.literal("§cRift Engine cooling down!"), true);
                            return;
                        }

                        if ("origin".equals(env)) {
                            ServerWorld overworld = context.server().getWorld(World.OVERWORLD);
                            if (overworld == null) return;
                            int topY = overworld.getTopY(Heightmap.Type.WORLD_SURFACE, 0, 0);
                            WARMUP_PLAYERS.put(player.getUuid(), 10);
                            WARMUP_TARGETS.put(player.getUuid(), new WarpTarget(0.5, topY + 0.1, 0.5, World.OVERWORLD.getValue().toString(), slotStack, time));
                            player.sendMessage(Text.literal("§5Channelling Origin Gate (Overworld 0, 0)..."), true);
                            return;
                        }

                        if ("hell".equals(env)) {
                            ServerWorld netherWorld = context.server().getWorld(World.NETHER);
                            if (netherWorld == null) return;

                            double targetX = player.getX();
                            double targetZ = player.getZ();
                            if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
                                targetX /= 8.0;
                                targetZ /= 8.0;
                            } else if (player.getWorld().getRegistryKey() == World.END) {
                                targetX = 0;
                                targetZ = 0;
                            }

                            int originBlockX = (int) Math.floor(targetX);
                            int originBlockZ = (int) Math.floor(targetZ);

                            int finalX = originBlockX;
                            int finalY = 64;
                            int finalZ = originBlockZ;
                            boolean foundSafeSpot = false;

                            searchLoop:
                            for (int r = 0; r <= 16; r++) {
                                for (int dx = -r; dx <= r; dx++) {
                                    for (int dz = -r; dz <= r; dz++) {
                                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                                        int currentX = originBlockX + dx;
                                        int currentZ = originBlockZ + dz;

                                        netherWorld.getChunkManager().getChunk(currentX >> 4, currentZ >> 4, net.minecraft.world.chunk.ChunkStatus.FULL, true);

                                        for (int y = 85; y > 31; y--) {
                                            BlockPos checkPos = new BlockPos(currentX, y, currentZ);
                                            BlockState stateCurrent = netherWorld.getBlockState(checkPos);
                                            BlockState stateAbove = netherWorld.getBlockState(checkPos.up());
                                            BlockState stateBelow = netherWorld.getBlockState(checkPos.down());

                                            if (stateCurrent.isAir() && stateAbove.isAir()) {
                                                if (stateBelow.isSolidBlock(netherWorld, checkPos.down()) && stateBelow.getFluidState().isEmpty()) {

                                                    boolean hasLavaDrop = false;
                                                    for (int drop = 1; drop <= 3; drop++) {
                                                        if (netherWorld.getFluidState(checkPos.down(drop)).isOf(net.minecraft.fluid.Fluids.LAVA)) {
                                                            hasLavaDrop = true;
                                                            break;
                                                        }
                                                    }

                                                    if (!hasLavaDrop) {
                                                        finalX = currentX;
                                                        finalY = y;
                                                        finalZ = currentZ;
                                                        foundSafeSpot = true;
                                                        break searchLoop;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (!foundSafeSpot) {
                                player.sendMessage(Text.literal("§6[Warning] Completely surrounded by lava ocean! Forcing safe coordinates at fallback Y=64."), true);
                            } else if (finalX != originBlockX || finalZ != originBlockZ) {
                                player.sendMessage(Text.literal("§5[Rift] Shifted destination away from open lava to nearest ledge!"), true);
                            }

                            WARMUP_PLAYERS.put(player.getUuid(), 10);
                            WARMUP_TARGETS.put(player.getUuid(), new WarpTarget(finalX + 0.5, finalY + 0.1, finalZ + 0.5, World.NETHER.getValue().toString(), slotStack, time));
                            player.sendMessage(Text.literal("§5Piercing veil to Nether coordinates..."), true);
                            return;
                        }

                        String playerUUID = player.getUuidAsString();
                        String internalKeyName = env.toLowerCase();

                        if (nbt.getBoolean(playerUUID + "_" + internalKeyName + "Bound")) {
                            double x = nbt.getDouble(playerUUID + "_" + internalKeyName + "X");
                            double y = nbt.getDouble(playerUUID + "_" + internalKeyName + "Y");
                            double z = nbt.getDouble(playerUUID + "_" + internalKeyName + "Z");
                            String dimStr = nbt.getString(playerUUID + "_" + internalKeyName + "Dim");

                            WARMUP_PLAYERS.put(player.getUuid(), 10);
                            WARMUP_TARGETS.put(player.getUuid(), new WarpTarget(x, y, z, dimStr, slotStack, time));
                            player.sendMessage(Text.literal("§5Opening rift stream..."), true);

                            net.minecraft.util.Identifier targetDimId = net.minecraft.util.Identifier.of(dimStr);
                            RegistryKey<World> targetDimKey = RegistryKey.of(RegistryKeys.WORLD, targetDimId);
                            ServerWorld targetWorld = context.server().getWorld(targetDimKey);

                            if (targetWorld != null) {
                                int centerChunkX = (int) Math.floor(x) >> 4;
                                int centerChunkZ = (int) Math.floor(z) >> 4;

                                BlockPos ticketAnchorPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

                                for (int cx = -1; cx <= 1; cx++) {
                                    for (int cz = -1; cz <= 1; cz++) {
                                        net.minecraft.util.math.ChunkPos targetChunkPos = new net.minecraft.util.math.ChunkPos(centerChunkX + cx, centerChunkZ + cz);

                                        targetWorld.getChunkManager().addTicket(
                                                net.minecraft.server.world.ChunkTicketType.PORTAL,
                                                targetChunkPos,
                                                3,
                                                ticketAnchorPos
                                        );
                                    }
                                }
                            }
                        }
                        else if (BIOMES.contains(env)) {
                            player.sendMessage(Text.literal("§6[Rift] Channel '" + env + "' has no spatial coordinate bound yet!"), true);
                            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
                        }
                        else {
                            player.damage(player.getWorld().getDamageSources().magic(), 4.0f);
                            player.sendMessage(Text.literal("§cRift Instability backfire! Incorrect frequency."), true);
                            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 0.5f, 1.8f);
                        }
                        return;
                    }
                }
            });
        });
    }

    @SuppressWarnings("MustBeClosedChecker")
    public static void tickWarmups(net.minecraft.server.MinecraftServer server) {
        for (UUID uuid : new java.util.ArrayList<>(WARMUP_PLAYERS.keySet())) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null || !player.isAlive()) {
                WARMUP_PLAYERS.remove(uuid);
                WARMUP_TARGETS.remove(uuid);
                continue;
            }

            int ticksLeft = WARMUP_PLAYERS.get(uuid) - 1;
            WarpTarget target = WARMUP_TARGETS.get(uuid);

            net.minecraft.server.world.ServerWorld currentWorld = player.getServerWorld();
            double px = player.getX();
            double py = player.getY() + 0.05;
            double pz = player.getZ();
            int points = 16;
            double radius = 0.8;

            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI * i) / points;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;
                currentWorld.spawnParticles(net.minecraft.particle.ParticleTypes.FIREWORK, px + dx, py, pz + dz, 1, 0, 0, 0, 0.01);
            }

            if (ticksLeft <= 0) {
                WARMUP_PLAYERS.remove(uuid);
                WARMUP_TARGETS.remove(uuid);

                net.minecraft.util.Identifier dimId = net.minecraft.util.Identifier.of(target.dimensionStr());
                RegistryKey<World> destinationKey = RegistryKey.of(RegistryKeys.WORLD, dimId);
                ServerWorld destinationWorld = server.getWorld(destinationKey);

                if (destinationWorld != null) {
                    player.teleport(destinationWorld, target.x, target.y, target.z, player.getYaw(), player.getPitch());

                    double tx = target.x;
                    double ty = target.y + 0.05;
                    double tz = target.z;
                    int destPoints = 16;
                    double destRadius = 0.8;

                    for (int j = 0; j < destPoints; j++) {
                        double angle = (2 * Math.PI * j) / destPoints;
                        double dx = Math.cos(angle) * destRadius;
                        double dz = Math.sin(angle) * destRadius;
                        destinationWorld.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, tx + dx, ty, tz + dz, 1, 0, 0, 0, 0.01);
                    }

                    NbtComponent comp = target.gemStack().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    NbtCompound nbt = comp.copyNbt();
                    nbt.putLong("LastWarpTime", target.currentWorldTime());
                    target.gemStack().set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    destinationWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.4f);
                }
            } else {
                WARMUP_PLAYERS.put(uuid, ticksLeft);
            }
        }
    }
}
