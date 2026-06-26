package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlessedCharge(int currentPoints, int requiredPoints) {
    public static final Codec<BlessedCharge> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("current").forGetter(BlessedCharge::currentPoints),
                    Codec.INT.fieldOf("required").forGetter(BlessedCharge::requiredPoints)
            ).apply(instance, BlessedCharge::new)
    );
}
