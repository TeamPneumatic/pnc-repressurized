package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public abstract class AssemblyRecipe extends PneumaticCraftRecipe {
    protected AssemblyRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Get the input ingredient.
     * @return the input ingredient
     */
    public abstract Ingredient getInput();

    /**
     * Get the number of items required/used
     * @return the number of items
     */
    public abstract int getInputAmount();

    /**
     * Get the output item for this recipe.
     * @return the output item
     */
    @Nonnull
    public abstract ItemStack getOutput();

    /**
     * Get the program required.
     * @return the program type
     */
    public abstract AssemblyProgramType getProgramType();

    /**
     * Check if the given stack is a valid input for this recipe.
     * @param stack input stack
     * @return true if valid, false otherwise
     */
    public abstract boolean matches(ItemStack stack);

    public enum AssemblyProgramType {
        DRILL, LASER, DRILL_LASER;

        public String getRegistryName() {
            return "assembly_program_" + this.toString().toLowerCase(Locale.ROOT);
        }

        public ResourceLocation getRecipeType() {
            switch (this) {
                case DRILL: return RL(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL);
                case LASER: return RL(PneumaticCraftRecipeTypes.ASSEMBLY_LASER);
                case DRILL_LASER: return RL(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER);
                default: throw new IllegalStateException("unknown type: " + this);
            }
        }
    }
}
