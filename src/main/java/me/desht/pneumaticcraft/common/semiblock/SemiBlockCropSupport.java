package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;

public class SemiBlockCropSupport extends SemiBlockBasic<TileEntity>{
    
    //From Minecraft wiki: For a given block, a random update occurs an average of once every 68.27 seconds.
    //0.002 = once very 25s.
    private static final double UPDATE_CHANCE = 0.002; //Chance every tick to tick the block
    
    public static final String ID = "crop_support";
    
    /**
     * Use this custom addition to prevent placement derpyness, where the crop gets placed on the tilled earth.
     */
    @Override
    public boolean canPlace() {
        IBlockState state = getBlockState();
        if(!state.getBlock().isAir(state, world, getPos()) && 
           !(state.getBlock() instanceof IPlantable)) return false;
        
        return canStay();
    }
    
    /**
     * When this block is not air, or the block below the crop is not air, it's OK.
     * @return
     */
    @Override
    public boolean canStay(){
        IBlockState state = getBlockState();
        if(!state.getBlock().isAir(state, world, getPos())) return true;
        
        BlockPos posBelow = getPos().offset(EnumFacing.DOWN);
        IBlockState stateBelow = world.getBlockState(posBelow);
        return !stateBelow.getBlock().isAir(stateBelow, world, posBelow);
    }
    
    @Override
    public void update(){
        super.update();
        
        if(!world.isRemote && world.rand.nextDouble() < UPDATE_CHANCE){
            IBlockState state = getBlockState();
            state.getBlock().updateTick(world, getPos(), state, world.rand);
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.VILLAGER_HAPPY, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, 0, 0, 0), world);
        }
    }
}
