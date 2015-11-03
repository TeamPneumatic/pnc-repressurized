package pneumaticCraft.common.heat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.lib.Log;

public class HeatExchangerManager{
    /**
     * Used for blocks like lava and ice.
     */
    private final Map<Block, IHeatExchanger> specialBlockExchangers = new HashMap<Block, IHeatExchanger>();
    private final IHeatExchangerLogic AIR_EXCHANGER = new HeatExchangerLogicConstant(295, 100);
    public static final double FLUID_RESISTANCE = 10;

    private static HeatExchangerManager INSTANCE = new HeatExchangerManager();

    public static HeatExchangerManager getInstance(){
        return INSTANCE;
    }

    public void init(){
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.ice, 263, 500);
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.packed_ice, 263, 500);
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.snow, 268, 1000);
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.torch, 1700, 100000);
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.fire, 1700, 1000);

        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for(Fluid fluid : fluids.values()) {
            if(fluid.getBlock() != null) {
                PneumaticRegistry.getInstance().registerBlockExchanger(fluid.getBlock(), fluid.getTemperature(), FLUID_RESISTANCE);
            }
        }
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.flowing_water, FluidRegistry.WATER.getTemperature(), 500);
        PneumaticRegistry.getInstance().registerBlockExchanger(Blocks.flowing_lava, FluidRegistry.LAVA.getTemperature(), 500);
    }

    public IHeatExchangerLogic getLogic(World world, int x, int y, int z, ForgeDirection side){
        if(!world.blockExists(x, y, z)) return null;
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof IHeatExchanger) {
            return ((IHeatExchanger)te).getHeatExchangerLogic(side);
        } else {
            if(world.isAirBlock(x, y, z)) {
                return AIR_EXCHANGER;
            } else {
                Block block = world.getBlock(x, y, z);
                if(block instanceof IHeatExchanger) {
                    return ((IHeatExchanger)block).getHeatExchangerLogic(side);
                } else {
                    IHeatExchanger exchanger = specialBlockExchangers.get(block);
                    return exchanger == null ? null : exchanger.getHeatExchangerLogic(side);
                }
            }
        }
    }

    public void registerBlockExchanger(Block block, IHeatExchanger heatExchanger){
        if(block == null) throw new IllegalArgumentException("block is null when trying to register a heat exchanger!");
        if(block instanceof IHeatExchanger) Log.warning("The block " + block.getUnlocalizedName() + " is implementing IHeatExchanger. Therefore you don't need to register it as such");
        if(specialBlockExchangers.containsKey(block)) {
            Log.error("The block " + block.getUnlocalizedName() + " was registered as heat exchanger already! It won't be added!");
        } else {
            specialBlockExchangers.put(block, heatExchanger);
        }
    }

}
