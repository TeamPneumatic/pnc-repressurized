package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidPneumaticCraft extends FlowingFluidBlock {

    public BlockFluidPneumaticCraft(Fluid fluid, Material material) {
        super(fluid, material);
        setTranslationKey(fluid.getName());
        // block registry names will be when blocks are registered in event handlers in Fluids
    }

    public BlockFluidPneumaticCraft(Fluid fluid) {
        this(fluid, Material.WATER);
    }
}
