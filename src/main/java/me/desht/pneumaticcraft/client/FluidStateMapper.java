package me.desht.pneumaticcraft.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {
    private final Fluid fluid;
    private final ModelResourceLocation location;

    public FluidStateMapper(Fluid fluid) {
        this.fluid = fluid;
        this.location = new ModelResourceLocation(RL("fluid"), fluid.getName());
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        return location;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return location;
    }
}
