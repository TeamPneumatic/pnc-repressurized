package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram.AssemblyProgramType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Collection;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class AssemblyRecipe implements IAssemblyRecipe {
    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputAmount;
    private final ItemStack output;
    private final Item program;

    public AssemblyRecipe(ResourceLocation id, @Nonnull Ingredient input, int inputAmount, @Nonnull ItemStack output, Item program) {
        Validate.isTrue(inputAmount > 0);
        Validate.isTrue(program instanceof ItemAssemblyProgram);
        this.id = id;
        this.input = input;
        this.inputAmount = inputAmount;
        this.output = output;
        this.program = program;
    }

    public AssemblyRecipe(ResourceLocation id, Ingredient input, ItemStack output, Item program) {
        this(id, input, 1, output, program);
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getInputAmount() {
        return inputAmount;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Item getProgram() {
        return program;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(id);
        input.write(buf);
        buf.writeVarInt(inputAmount);
        buf.writeItemStack(output);
        buf.writeItemStack(new ItemStack(program));
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack) && stack.getCount() >= inputAmount;
    }

    /**
     * Categorise assembly recipes by subtype; whether they require a laser or drill.  Also set up chained recipes;
     * see {@link #calculateAssemblyChain()}.
     *
     * @param recipes all known assembly recipes
     */
    static void setupRecipeSubtypes(Collection<IAssemblyRecipe> recipes) {
        for (IAssemblyRecipe recipe : recipes) {
            AssemblyProgramType type = ((ItemAssemblyProgram) recipe.getProgram()).getProgramType();
            switch (type) {
                case LASER:
                    PneumaticCraftRecipes.assemblyLaserRecipes.put(recipe.getId(), recipe);
                    break;
                case DRILL:
                    PneumaticCraftRecipes.assemblyDrillRecipes.put(recipe.getId(), recipe);
                    break;
            }
        }
        calculateAssemblyChain();
    }

    /**
     * Work out which recipes can be chained.  E.g. if laser recipe makes B from A, and drill recipe makes C from B,
     * then add a laser/drill recipe to make C from A. Takes into account the number of inputs & outputs from each step.
     */
    private static void calculateAssemblyChain() {
        for (IAssemblyRecipe r1 : PneumaticCraftRecipes.assemblyDrillRecipes.values()) {
            for (IAssemblyRecipe r2 : PneumaticCraftRecipes.assemblyLaserRecipes.values()) {
                if (r2.getInput().test(r1.getOutput())
                        && r1.getOutput().getCount() % r2.getInputAmount() == 0
                        && r2.getOutput().getMaxStackSize() >= r2.getOutput().getCount() * (r1.getOutput().getCount() / r2.getInputAmount())) {
                    ItemStack output = r2.getOutput().copy();
                    output.setCount(output.getCount() * (r1.getOutput().getCount() / r2.getInputAmount()));
                    String name = r1.getId().getPath() + "/" + r2.getId().getPath();
                    ResourceLocation id = RL(name);
                    PneumaticCraftRecipes.assemblyLaserDrillRecipes.put(id, new AssemblyRecipe(id, r1.getInput(), output, ModItems.ASSEMBLY_PROGRAM_DRILL_LASER));
                }
            }
        }
    }

//    static void addDrillRecipe(Object input, Object output) {
//        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyDrillRecipe(input, output);
//    }
//
//    static void addLaserRecipe(Object input, Object output) {
//        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyLaserRecipe(input, output);
//    }
//
//    public static AssemblyRecipe findRecipeForOutput(ItemStack result) {
//        for (AssemblyRecipe recipe : AssemblyRecipe.drillLaserRecipes) {
//            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
//                return recipe;
//            }
//        }
//        for (AssemblyRecipe recipe : AssemblyRecipe.drillRecipes) {
//            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
//                return recipe;
//            }
//        }
//        for (AssemblyRecipe recipe : AssemblyRecipe.laserRecipes) {
//            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
//                return recipe;
//            }
//        }
//        return null;
//    }
}
