package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Collections;
import java.util.Map;

@Document("mods/PneumaticCraft/BlockHeatProperties")
@ZenRegister
@ZenCodeType.Name("mods.pneumaticcraft.BlockHeatProperties")
public class BlockHeatProperties implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, Block block, int temperature, double thermalResistance) {
        addRecipe(name, block, Collections.emptyMap(), temperature, thermalResistance);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, Block block, Map<String,String> matchProps, int temperature, double thermalResistance) {
        if (matchProps.isEmpty()) {
            CraftTweakerAPI.apply(new ActionAddRecipe(this,
                    new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                            block, temperature, thermalResistance)
            ));
        } else {
            CraftTweakerAPI.apply(new ActionAddRecipe(this,
                    new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                            block,
                            null, null, null, null,
                            0, temperature, thermalResistance, matchProps, "")
            ));
        }
    }

    @ZenCodeType.Method
    public void addRecipe(String name, Block block, Map<String,String> matchProps,
                          int temperature, double thermalResistance, int heatCapacity,
                          @ZenCodeType.Nullable BlockState transformHot, @ZenCodeType.Nullable BlockState transformHotFlowing,
                          @ZenCodeType.Nullable BlockState transformCold, @ZenCodeType.Nullable BlockState transformColdFlowing,
                          @ZenCodeType.OptionalString String descriptionKey) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        block,
                        transformHot, transformHotFlowing,
                        transformCold, transformColdFlowing,
                        heatCapacity, temperature, thermalResistance,
                        matchProps,
                        descriptionKey)
        ));
    }

    @Override
    public IRecipeType<HeatPropertiesRecipeImpl> getRecipeType() {
        return PneumaticCraftRecipeType.HEAT_PROPERTIES;
    }
}
