package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class SemiBlockCropSupport extends SemiBlockBasic<TileEntity>{
    public static final ResourceLocation ID = RL("crop_support");
    
    public SemiBlockCropSupport(){
        super(TileEntity.class);
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockState state = getBlockState();
        return (state.getBlock().isAir(state, world, getPos()) || state.getBlock() instanceof IPlantable) && canStay();
    }

    @Override
    public boolean canStay(){
        BlockState state = getBlockState();
        if(!state.getBlock().isAir(state, world, getPos())) return true;
        
        BlockPos posBelow = getPos().offset(Direction.DOWN);
        BlockState stateBelow = world.getBlockState(posBelow);
        return !stateBelow.getBlock().isAir(stateBelow, world, posBelow);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void tick(){
        super.tick();

        if (world.rand.nextDouble() < PNCConfig.Common.Machines.cropSticksGrowthBoostChance) {
            if(!world.isRemote) {
                getBlockState().tick(world, getPos(), world.rand);
            } else {
                world.addParticle(ParticleTypes.HAPPY_VILLAGER, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, 0, 0, 0);
            }
        }
    }
}
