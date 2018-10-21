package me.desht.pneumaticcraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class AirParticle extends Particle {
    public static final ResourceLocation AIR_PARTICLE_TEXTURE = RL("particle/air_particle");

    AirParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        particleMaxAge = 50;
        particleAlpha = 0.1f;

        motionX = xSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionY = ySpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionZ = zSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(AIR_PARTICLE_TEXTURE.toString());
        setParticleTexture(sprite);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public boolean shouldDisableDepth() {
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isAirBlock(new BlockPos(posX, posY, posZ)) || onGround) {
            setExpired();
        }

        // fades out and gets bigger as it gets older
        multipleParticleScaleBy(1.04f);
        particleAlpha *= 0.975;

        if (world.rand.nextInt(5) == 0) {
            motionX += world.rand.nextDouble() * 0.1 - 0.05;
        }
        if (world.rand.nextInt(5) == 0) {
            motionY += world.rand.nextDouble() * 0.1 - 0.05;
        }
        if (world.rand.nextInt(5) == 0) {
            motionY += world.rand.nextDouble() * 0.1 - 0.05;
        }
    }
}
