package pneumaticCraft.common.heat.behaviour;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.common.heat.HeatExchangerManager;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketPlaySound;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.util.FluidUtils;

public abstract class HeatBehaviourLiquidTransition extends HeatBehaviourLiquid{
    private double extractedHeat;
    private double maxExchangedHeat;
    private int fluidTemp = -1;

    @Override
    public boolean isApplicable(){
        Fluid fluid = getFluid();
        return fluid != null && fluid.getTemperature() >= getMinFluidTemp() && fluid.getTemperature() <= getMaxFluidTemp();
    }

    protected abstract int getMinFluidTemp();

    protected abstract int getMaxFluidTemp();

    protected abstract int getMaxExchangedHeat();

    protected abstract Block getTransitionedSourceBlock();

    protected abstract Block getTransitionedFlowingBlock();

    protected abstract boolean transitionOnTooMuchExtraction();

    @Override
    public void update(){
        if(fluidTemp == -1) {
            fluidTemp = getFluid().getTemperature();
            maxExchangedHeat = getMaxExchangedHeat() * (HeatExchangerManager.FLUID_RESISTANCE + getHeatExchanger().getThermalResistance());
        }
        extractedHeat += fluidTemp - getHeatExchanger().getTemperature();
        if(transitionOnTooMuchExtraction() ? extractedHeat > maxExchangedHeat : extractedHeat < -maxExchangedHeat) {
            transformSourceBlock(getTransitionedSourceBlock(), getTransitionedFlowingBlock());
            extractedHeat -= maxExchangedHeat;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setDouble("extractedHeat", extractedHeat);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        extractedHeat = tag.getDouble("extractedHeat");
    }

    protected void transformSourceBlock(Block turningBlockSource, Block turningBlockFlowing){
        if(FluidUtils.isSourceBlock(getWorld(), getX(), getY(), getZ())) {
            getWorld().setBlock(getX(), getY(), getZ(), turningBlockSource);
            onLiquidTransition(getX(), getY(), getZ());
        } else {
            Set<ChunkPosition> traversed = new HashSet<ChunkPosition>();
            Stack<ChunkPosition> pending = new Stack<ChunkPosition>();
            pending.push(new ChunkPosition(getX(), getY(), getZ()));
            while(!pending.isEmpty()) {
                ChunkPosition pos = pending.pop();
                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    ChunkPosition newPos = new ChunkPosition(pos.chunkPosX + d.offsetX, pos.chunkPosY + d.offsetY, pos.chunkPosZ + d.offsetZ);
                    Block checkingBlock = getWorld().getBlock(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ);
                    if((checkingBlock == getBlock() || getBlock() == Blocks.flowing_water && checkingBlock == Blocks.water || getBlock() == Blocks.flowing_lava && checkingBlock == Blocks.lava) && traversed.add(newPos)) {
                        if(FluidUtils.isSourceBlock(getWorld(), newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ)) {
                            getWorld().setBlock(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ, turningBlockSource);
                            onLiquidTransition(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ);
                            return;
                        } else {
                            getWorld().setBlock(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ, turningBlockFlowing);
                            onLiquidTransition(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ);
                            pending.push(newPos);
                        }
                    }
                }
            }
        }
    }

    protected void onLiquidTransition(int x, int y, int z){
        NetworkHandler.sendToAllAround(new PacketPlaySound("random.fizz", x + 0.5, y + 0.5, z + 0.5, 0.5F, 2.6F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.8F, true), getWorld());
        for(int i = 0; i < 8; i++) {
            double randX = x + getWorld().rand.nextDouble();
            double randZ = z + getWorld().rand.nextDouble();
            NetworkHandler.sendToAllAround(new PacketSpawnParticle("largesmoke", randX, y + 1, randZ, 0, 0, 0), getWorld());
        }
    }
}
