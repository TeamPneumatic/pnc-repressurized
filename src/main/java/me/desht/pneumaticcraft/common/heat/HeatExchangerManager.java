package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.common.config.BlockHeatPropertiesConfig;
import me.desht.pneumaticcraft.common.config.BlockHeatPropertiesConfig.CustomHeatEntry;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;

public class HeatExchangerManager implements IHeatRegistry {
    // Used to add thermal properties to vanilla blocks or non-tile-entity modded blocks
    private final Map<Block, IHeatExchanger> specialBlockExchangers = new HashMap<>();
    private final Map<String, IHeatExchanger> specialBlockvariantExchangers = new HashMap<>();

//    static final IHeatExchangerLogic AIR_EXCHANGER = new HeatExchangerLogicConstant(295, 100);

    private static final HeatExchangerManager INSTANCE = new HeatExchangerManager();

    public static HeatExchangerManager getInstance() {
        return INSTANCE;
    }

    public void onPostInit() {
        BlockHeatPropertiesConfig.INSTANCE.getCustomHeatEntries().values().forEach(this::registerCustomHeatEntry);

        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : fluids.values()) {
            if (fluid.getBlock() != null && !specialBlockExchangers.containsKey(fluid.getBlock())) {
                CustomHeatEntry entry = BlockHeatPropertiesConfig.INSTANCE.getCustomHeatEntry(fluid.getBlock().getDefaultState());
                if (entry != null) {
                    registerBlockExchanger(fluid.getBlock(), entry.getTemperature(), entry.getThermalResistance());
                } else {
                    Log.warning("unable to retrieve custom heat entry for fluid " + fluid.getName() + " - block: " + fluid.getBlock());
                }
            }
        }
        // the vanilla flowing blocks aren't in the forge fluid registry...
        registerBlockExchanger(Blocks.FLOWING_LAVA, specialBlockExchangers.get(Blocks.LAVA));
        registerBlockExchanger(Blocks.FLOWING_WATER, specialBlockExchangers.get(Blocks.WATER));
    }


    private void registerCustomHeatEntry(CustomHeatEntry rec) {
        if (rec.isDefaultState()) {
            // all states of this block type
            registerBlockExchanger(rec.getBlockState().getBlock(), rec.getTemperature(), rec.getThermalResistance());
        } else {
            // a specific blockstate - use where modded blocks have subtypes
            registerBlockExchanger(rec.getBlockState(), rec.getTemperature(), rec.getThermalResistance());
        }
    }

    public IHeatExchangerLogic getLogic(World world, BlockPos pos, EnumFacing side) {
        if (!world.isBlockLoaded(pos)) return null;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IHeatExchanger) {
            return ((IHeatExchanger) te).getHeatExchangerLogic(side);
        } else {
            if (world.isAirBlock(pos)) {
                return HeatExchangerLogicAmbient.atPosition(world, pos);
//                return AIR_EXCHANGER;
            } else {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof IHeatExchanger) {
                    return ((IHeatExchanger) block).getHeatExchangerLogic(side);
                } else {
                    IHeatExchanger exchanger = getSpecialBlockExchanger(state);
                    return exchanger == null ? null : exchanger.getHeatExchangerLogic(side);
                }
            }
        }
    }

    private IHeatExchanger getSpecialBlockExchanger(IBlockState state) {
        String key = state.getBlock().getRegistryName() + ":" + state.getBlock().getMetaFromState(state);
        IHeatExchanger exchanger = specialBlockvariantExchangers.get(key);
        return exchanger == null ? specialBlockExchangers.get(state.getBlock()) : exchanger;
    }

    // todo this can go away in 1.13 when it will be purely block-based (yay flattening)
    private void registerBlockExchanger(IBlockState state, IHeatExchanger heatExchanger) {
        Block block = state.getBlock();
        if (block instanceof IHeatExchanger)
            Log.warning("The block " + block.getTranslationKey() + " is implementing IHeatExchanger. Therefore you don't need to register it as such");
        String key = block.getRegistryName() + ":" + block.getMetaFromState(state);
        if (specialBlockvariantExchangers.containsKey(key)) {
            Log.error("The block " + key + " was registered as heat exchanger already! It won't be added!");
        } else {
            specialBlockvariantExchangers.put(key, heatExchanger);
        }
    }

    private void registerBlockExchanger(Block block, IHeatExchanger heatExchanger) {
        if (block == null)
            throw new IllegalArgumentException("block is null when trying to register a heat exchanger!");
        if (block instanceof IHeatExchanger)
            Log.warning("The block " + block.getTranslationKey() + " is implementing IHeatExchanger. Therefore you don't need to register it as such");
        if (specialBlockExchangers.containsKey(block)) {
            Log.error("The block " + block.getTranslationKey() + " was registered as heat exchanger already! It won't be added!");
        } else {
            specialBlockExchangers.put(block, heatExchanger);
        }
    }

    private void registerBlockExchanger(Block block, IHeatExchangerLogic heatExchangerLogic) {
        registerBlockExchanger(block, new SimpleHeatExchanger(heatExchangerLogic));
    }

    private void registerBlockExchanger(IBlockState state, IHeatExchangerLogic heatExchangerLogic) {
        registerBlockExchanger(state, new SimpleHeatExchanger(heatExchangerLogic));
    }

    @Override
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance) {
        registerBlockExchanger(block, new HeatExchangerLogicConstant(temperature, thermalResistance));
    }

    @Override
    public void registerBlockExchanger(IBlockState state, double temperature, double thermalResistance) {
        registerBlockExchanger(state, new HeatExchangerLogicConstant(temperature, thermalResistance));
    }

    @Override
    public void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour) {
        HeatBehaviourManager.getInstance().registerBehaviour(heatBehaviour);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic() {
        return new HeatExchangerLogicTicking();
    }


    public static class TemperatureData {
        private final Double[] temp = new Double[7];

        private boolean isMultisided = true;

        public TemperatureData(IHeatExchanger heatExchanger) {
            Arrays.fill(temp, null);

            Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
            IHeatExchangerLogic logic = null;
            for (EnumFacing face : EnumFacing.VALUES) {
                logic = heatExchanger.getHeatExchangerLogic(face);
                if (logic != null) {
                    if (heatExchangers.contains(logic)) {
                        isMultisided = false;
                        break;
                    } else {
                        heatExchangers.add(logic);
                    }
                }
            }

            if (isMultisided) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    logic = heatExchanger.getHeatExchangerLogic(face);
                    if (logic != null) {
                        temp[face.ordinal()] = logic.getTemperature();
                    }
                }
            } else if (logic != null) {
                temp[6] = logic.getTemperature();
            }
        }

        public boolean isMultisided() {
            return isMultisided;
        }

        public double getTemperature(EnumFacing face) {
            return face == null ? temp[6] : temp[face.ordinal()];
        }

        public boolean hasData(EnumFacing face) {
            return face == null ? temp[6] != null : temp[face.ordinal()] != null;
        }
    }
}
