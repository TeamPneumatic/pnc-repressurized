package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/HeatFrameCooling")
@ZenCodeType.Name("mods.pneumaticcraft.heatframecooling")
@ZenRegister
public class HeatFrameCooling implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredient input, IItemStack output, int temperature, @ZenCodeType.OptionalFloat float bonusMult, @ZenCodeType.OptionalFloat float bonusLimit) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new HeatFrameCoolingRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        input.asVanillaIngredient(),
                        temperature,
                        output.getInternal(),
                        bonusMult, bonusLimit)
        ));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient inputFluid, IItemStack output, int temperature, @ZenCodeType.OptionalFloat float bonusMult, @ZenCodeType.OptionalFloat float bonusLimit) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new HeatFrameCoolingRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toFluidIngredient(inputFluid),
                        temperature,
                        output.getInternal(),
                        bonusMult, bonusLimit)
        ));
    }

    @Override
    public IRecipeType<HeatFrameCoolingRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.HEAT_FRAME_COOLING;
    }
}
