package eyeliss.particle.mod.client.render;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WitherEntityRenderer;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;

public class UmberwitherRenderer extends WitherEntityRenderer {
    // Defines paths for your standard and invulnerable (shielded) textures
    private static final Identifier TEXTURE = Identifier.of(EyelisssParticleMod.MOD_ID, "textures/entity/umberwither/umberwither.png");
    private static final Identifier INVULNERABLE_TEXTURE = Identifier.of(EyelisssParticleMod.MOD_ID, "textures/entity/umberwither/umberwither_invulnerable.png");

    public UmberwitherRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WitherEntity witherEntity) {
        // Check if the boss is currently in its charging/invulnerable state
        int invulTime = witherEntity.getInvulnerableTimer();
        if (invulTime > 0 && (invulTime > 80 || invulTime / 5 % 2 != 1)) {
            return INVULNERABLE_TEXTURE;
        }
        return TEXTURE;
    }
}