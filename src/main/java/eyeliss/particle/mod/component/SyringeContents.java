package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SyringeContents(List<SyringeContents.Payload> payloads, int durationLeft) {

    public record Payload(String effectId, int amplifier) {
        public static final Codec<Payload> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("id").forGetter(Payload::effectId),
                        Codec.INT.fieldOf("amplifier").forGetter(Payload::amplifier)
                ).apply(instance, Payload::new)
        );
    }

    public static final Codec<SyringeContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Payload.CODEC.listOf().fieldOf("payloads").forGetter(SyringeContents::payloads),
                    Codec.INT.fieldOf("duration_left").forGetter(SyringeContents::durationLeft)
            ).apply(instance, SyringeContents::new)
    );

    public static final SyringeContents EMPTY = new SyringeContents(List.of(new Payload("minecraft:empty", 0)), 0);
}