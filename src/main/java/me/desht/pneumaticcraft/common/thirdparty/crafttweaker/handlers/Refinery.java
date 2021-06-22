package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.RefineryRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/Refinery")
@ZenCodeType.Name("mods.pneumaticcraft.refinery")
@ZenRegister
public class Refinery implements IRecipeManager {
	@ZenCodeType.Method
	public void addRecipe(String name, CTFluidIngredient input, IFluidStack[] outputs, int minTemp, @ZenCodeType.OptionalInt(Integer.MAX_VALUE) int maxTemp) {
		CraftTweakerAPI.apply(new ActionAddRecipe(this,
				new RefineryRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
						CTUtils.toFluidIngredient(input),
						TemperatureRange.of(minTemp, maxTemp),
						CTUtils.toFluidStacks(outputs))
		));
	}

	@Override
	public IRecipeType<RefineryRecipeImpl> getRecipeType() {
		return PneumaticCraftRecipeType.REFINERY;
	}
}
