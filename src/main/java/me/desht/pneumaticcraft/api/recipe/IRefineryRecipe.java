package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IRefineryRecipe extends IModRecipe {
    int MAX_OUTPUTS = 4;

    FluidStack getInput();

    List<FluidStack> getOutputs();

    TemperatureRange getOperatingTemp();

    /**
     * Create a standard Refinery recipe.  Note that multiple recipes with the same input fluid may exist, provided that
     * there is a different number of output fluids.  The refinery will use the recipe with the largest number of outputs.
     *
     * @param id unique ID for this recipe
     * @param input the input fluid
     * @param operatingTemp a temperature range required for the recipe to craft
     * @param outputs the output fluids
     * @return a basic Refinery recipe
     */
    static IRefineryRecipe basicRecipe(ResourceLocation id, FluidStack input, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipe(id, input, operatingTemp, outputs);
    }

    /**
     * Used for client-side sync'ing of recipes: do not call directly!
     * @param buf a packet buffer
     * @return a deserialised recipe
     */
    static IRefineryRecipe read(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        FluidStack input = FluidStack.readFromPacket(buf);
        TemperatureRange range = TemperatureRange.of(buf.readVarInt(), buf.readVarInt());
        int nOutputs = buf.readVarInt();
        FluidStack[] outputs = new FluidStack[nOutputs];
        for (int i = 0; i < nOutputs; i++) {
            outputs[i] = FluidStack.readFromPacket(buf);
        }
        return new RefineryRecipe(id, input, range, outputs);
    }
}
