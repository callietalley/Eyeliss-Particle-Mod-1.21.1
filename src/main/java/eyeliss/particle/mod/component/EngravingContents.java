package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EngravingContents(String engravingId, int level) {

    public static final Codec<EngravingContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(EngravingContents::engravingId),
                    Codec.INT.fieldOf("level").forGetter(EngravingContents::level)
            ).apply(instance, EngravingContents::new)
    );
}
