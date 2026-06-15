package eyeliss.particle.mod.util;

import eyeliss.particle.mod.item.HarmoniousEssenceItem;
import eyeliss.particle.mod.sound.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

@Environment(EnvType.CLIENT)
public class ClientSoundTracker {

    private static MovingHarmoniousSound activeSoundInstance = null;

    public static boolean isSoundPlaying() {
        // Double-check with Minecraft's engine to see if the sound is genuinely still playing audio channels
        if (activeSoundInstance != null && !MinecraftClient.getInstance().getSoundManager().isPlaying(activeSoundInstance)) {
            activeSoundInstance = null; // Clean up memory if it finished naturally
        }
        return activeSoundInstance != null && !activeSoundInstance.isDone();
    }

    public static void playHarmoniousSound(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();

        stopHarmoniousSound();

        activeSoundInstance = new MovingHarmoniousSound(player);
        client.getSoundManager().play(activeSoundInstance);
    }

    public static void stopHarmoniousSound() {
        if (activeSoundInstance != null) {
            activeSoundInstance.forceStop();
            activeSoundInstance = null;
        }
    }

    private static class MovingHarmoniousSound extends MovingSoundInstance {
        private final PlayerEntity player;

        protected MovingHarmoniousSound(PlayerEntity player) {
            super(ModSounds.HARMONIOUS_ESSENCE_EVENT, SoundCategory.PLAYERS, SoundInstance.createRandom());
            this.player = player;
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.pitch = 1.0f;

            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();
        }

        @Override
        public void tick() {
            // Check if the player dropped the item out of their hands entirely
            boolean holdingEssence = player.getMainHandStack().getItem() instanceof HarmoniousEssenceItem
                    || player.getOffHandStack().getItem() instanceof HarmoniousEssenceItem;

            // Stop the stream if the player dies, leaves the world, or drops the item
            if (this.player.isRemoved() || !holdingEssence) {
                this.forceStop();
                return;
            }

            // Continuously track the audio source position to match the moving player
            this.x = (float) this.player.getX();
            this.y = (float) this.player.getY();
            this.z = (float) this.player.getZ();
        }

        public void forceStop() {
            this.setDone();
        }
    }
}
