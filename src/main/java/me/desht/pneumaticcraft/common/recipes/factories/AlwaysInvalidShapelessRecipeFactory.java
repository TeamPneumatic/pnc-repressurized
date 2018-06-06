package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Registered to Forge by the _factories.json in the recipes resource folder.
 * 
 * This is used for recipes that are registered for the crafting book / mods like JEI,
 * while this recipe may not be used, because NBT needs to be preserved for example.
 * @author Maarten
 *
 */
public class AlwaysInvalidShapelessRecipeFactory implements IRecipeFactory{

    @Override
    public IRecipe parse(JsonContext context, JsonObject json){
        ShapelessOreRecipe dummy = ShapelessOreRecipe.factory(context, json);
        return new AlwaysInvalidShapelessOreRecipe(dummy);
    }
    
    public class AlwaysInvalidShapelessOreRecipe extends ShapelessOreRecipe{

        public AlwaysInvalidShapelessOreRecipe(ShapelessOreRecipe dummy){
            super(new ResourceLocation(dummy.getGroup()), dummy.getIngredients(), dummy.getRecipeOutput());
        }
        
        @Override
        public boolean matches(InventoryCrafting inv, World world){
            return false;
        }
    }

}
