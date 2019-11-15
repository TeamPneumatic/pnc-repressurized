package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BlockFluidPneumaticCraft extends FlowingFluidBlock {

    public BlockFluidPneumaticCraft(Supplier<? extends FlowingFluid> supplier, Properties props, String name) {
        super(supplier, props);
        setRegistryName(RL(name));
    }

//    public BlockFluidPneumaticCraft(Fluid fluid, Material material) {
//        super(fluid, material);
//        setTranslationKey(fluid.getName());
//        // block registry names will be when blocks are registered in event handlers in Fluids
//    }
//
//    public BlockFluidPneumaticCraft(Fluid fluid) {
//        this(fluid, Material.WATER);
//    }
}
