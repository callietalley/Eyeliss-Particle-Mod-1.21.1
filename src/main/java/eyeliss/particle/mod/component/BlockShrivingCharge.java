package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlockShrivingCharge(int currentBlocks, int requiredBlocks) {
    public static final Codec<BlockShrivingCharge> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("current_blocks").forGetter(BlockShrivingCharge::currentBlocks),
                    Codec.INT.fieldOf("required_blocks").forGetter(BlockShrivingCharge::requiredBlocks)
            ).apply(instance, BlockShrivingCharge::new)
    );
}
