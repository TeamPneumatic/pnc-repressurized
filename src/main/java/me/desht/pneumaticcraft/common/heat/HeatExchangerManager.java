package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashMap;
import java.util.Map;

public class HeatExchangerManager implements IHeatRegistry {
    /**
     * Used for blocks like lava and ice.
     */
    private final Map<Block, IHeatExchanger> specialBlockExchangers = new HashMap<Block, IHeatExchanger>();
    private final IHeatExchangerLogic AIR_EXCHANGER = new HeatExchangerLogicConstant(295, 100);
    public static final double FLUID_RESISTANCE = 10;

    private static HeatExchangerManager INSTANCE = new HeatExchangerManager();

    public static HeatExchangerManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        registerBlockExchanger(Blocks.ICE, 263, 500);
        registerBlockExchanger(Blocks.PACKED_ICE, 263, 500);
        registerBlockExchanger(Blocks.SNOW, 268, 1000);
        registerBlockExchanger(Blocks.TORCH, 1700, 100000);
        registerBlockExchanger(Blocks.FIRE, 1700, 1000);

        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : fluids.values()) {
            if (fluid.getBlock() != null) {
                registerBlockExchanger(fluid.getBlock(), fluid.getTemperature(), FLUID_RESISTANCE);
            }
        }
        registerBlockExchanger(Blocks.FLOWING_WATER, FluidRegistry.WATER.getTemperature(), 500);
        registerBlockExchanger(Blocks.FLOWING_LAVA, FluidRegistry.LAVA.getTemperature(), 500);
    }

    public IHeatExchangerLogic getLogic(World world, BlockPos pos, EnumFacing side) {
        if (!world.isBlockLoaded(pos)) return null;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IHeatExchanger) {
            return ((IHeatExchanger) te).getHeatExchangerLogic(side);
        } else {
            if (world.isAirBlock(pos)) {
                return AIR_EXCHANGER;
            } else {
                Block block = world.getBlockState(pos).getBlock();
                if (block instanceof IHeatExchanger) {
                    return ((IHeatExchanger) block).getHeatExchangerLogic(side);
                } else {
                    IHeatExchanger exchanger = specialBlockExchangers.get(block);
                    return exchanger == null ? null : exchanger.getHeatExchangerLogic(side);
                }
            }
        }
    }

    public void registerBlockExchanger(Block block, IHeatExchanger heatExchanger) {
        if (block == null)
            throw new IllegalArgumentException("block is null when trying to register a heat exchanger!");
        if (block instanceof IHeatExchanger)
            Log.warning("The block " + block.getUnlocalizedName() + " is implementing IHeatExchanger. Therefore you don't need to register it as such");
        if (specialBlockExchangers.containsKey(block)) {
            Log.error("The block " + block.getUnlocalizedName() + " was registered as heat exchanger already! It won't be added!");
        } else {
            specialBlockExchangers.put(block, heatExchanger);
        }
    }

    public void registerBlockExchanger(Block block, IHeatExchangerLogic heatExchangerLogic) {
        registerBlockExchanger(block, new SimpleHeatExchanger(heatExchangerLogic));
    }

    @Override
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance) {
        registerBlockExchanger(block, new HeatExchangerLogicConstant(temperature, thermalResistance));
    }

    @Override
    public void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour) {
        HeatBehaviourManager.getInstance().registerBehaviour(heatBehaviour);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic() {
        return new HeatExchangerLogic();
    }

}
