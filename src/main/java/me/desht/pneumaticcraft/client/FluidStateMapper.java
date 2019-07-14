package me.desht.pneumaticcraft.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {
    private final ModelResourceLocation location;

    FluidStateMapper(Fluid fluid) {
        this.location = new ModelResourceLocation(RL("fluid"), fluid.getName());
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        return location;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(BlockState state) {
        return location;
    }
}
