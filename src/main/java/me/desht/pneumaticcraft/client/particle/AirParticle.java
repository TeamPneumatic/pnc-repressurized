package me.desht.pneumaticcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class AirParticle extends SpriteTexturedParticle {
    private final IAnimatedSprite sprite;

    private AirParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float scale, IAnimatedSprite sprite) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        this.sprite = sprite;
//        if (sprite == null) {
//            Calendar calendar = Calendar.getInstance();
//            if (calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) >= 31
//                || calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) <= 2) {
//                sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(AIR_PARTICLE_TEXTURE2.toString());
//            } else {
//                sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(AIR_PARTICLE_TEXTURE.toString());
//            }
//        }

        maxAge = 50;
        particleAlpha = 0.1f;
        particleScale = scale;

        motionX = xSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionY = ySpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionZ = zSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;

        selectSpriteWithAge(sprite);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isAirBlock(new BlockPos(posX, posY, posZ)) || onGround) {
            setExpired();
        }

        // fades out and gets bigger as it gets older
        selectSpriteWithAge(sprite);
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

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<AirParticleData> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle makeParticle(AirParticleData airParticleData, World world, double x, double y, double z, double dx, double dy, double dz) {
            AirParticle p = new AirParticle(world, x, y, z, dx, dy, dz, 0.2f, spriteSet);
            p.setAlphaF(airParticleData.getAlpha());
            return p;
        }
    }
}
