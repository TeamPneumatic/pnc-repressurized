package me.desht.pneumaticcraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Calendar;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class AirParticle extends Particle {
    public static final ResourceLocation AIR_PARTICLE_TEXTURE = RL("particle/air_particle");
    public static final ResourceLocation AIR_PARTICLE_TEXTURE2 = RL("particle/air_particle2");

    private static TextureAtlasSprite sprite = null;

    AirParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        if (sprite == null) {
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) >= 31
                || calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) <= 2) {
                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(AIR_PARTICLE_TEXTURE2.toString());
            } else {
                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(AIR_PARTICLE_TEXTURE.toString());
            }
        }
        particleMaxAge = 50;
        particleAlpha = 0.1f;

        motionX = xSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionY = ySpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionZ = zSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;

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
