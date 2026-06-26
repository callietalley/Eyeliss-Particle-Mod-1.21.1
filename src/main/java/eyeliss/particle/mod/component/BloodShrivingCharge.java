package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BloodShrivingCharge(int currentKills, int requiredKills) {
    public static final Codec<BloodShrivingCharge> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("current_kills").forGetter(BloodShrivingCharge::currentKills),
                    Codec.INT.fieldOf("required_kills").forGetter(BloodShrivingCharge::requiredKills)
            ).apply(instance, BloodShrivingCharge::new)
    );
}
