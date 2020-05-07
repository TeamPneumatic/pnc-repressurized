package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class AssemblyRecipeImpl extends AssemblyRecipe {
    private final Ingredient input;
    private final ItemStack output;
    private final AssemblyProgramType program;

    public AssemblyRecipeImpl(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output, AssemblyProgramType program) {
        super(id);

        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getInputAmount() {
        return input.getMatchingStacks().length > 0 ? input.getMatchingStacks()[0].getCount() : 0;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public AssemblyProgramType getProgramType() {
        return program;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack) && stack.getCount() >= getInputAmount();
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(output);
        buffer.writeVarInt(program.ordinal());
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        switch (getProgramType()) {
            case LASER: return ModRecipes.ASSEMBLY_LASER.get();
            case DRILL: return ModRecipes.ASSEMBLY_DRILL.get();
            default: throw new IllegalStateException("invalid program type: " + getProgramType());
        }
    }

    @Override
    public IRecipeType<?> getType() {
        switch (getProgramType()) {
            case DRILL: return PneumaticCraftRecipeType.ASSEMBLY_DRILL;
            case LASER: return PneumaticCraftRecipeType.ASSEMBLY_LASER;
            case DRILL_LASER: return PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER;
        }
        throw new IllegalStateException("invalid program type: " + getProgramType());
    }

    /**
     * Work out which recipes can be chained.  E.g. if laser recipe makes B from A, and drill recipe makes C from B,
     * then add a synthetic laser/drill recipe to make C from A. Takes into account the number of inputs & outputs
     * from each step.
     *
     * @param drillRecipes all known drill recipes
     * @param laserRecipes all known laser recipes
     * @return a map (recipeId -> recipe) of all synthetic laser/drill recipes
     */
    public static Map<ResourceLocation, AssemblyRecipeImpl> calculateAssemblyChain(Collection<AssemblyRecipe> drillRecipes, Collection<AssemblyRecipe> laserRecipes) {
        Map<ResourceLocation, AssemblyRecipeImpl> drillLaser = new HashMap<>();
        for (AssemblyRecipe r1 : drillRecipes) {
            for (AssemblyRecipe r2 : laserRecipes) {
                if (r2.getInput().test(r1.getOutput())
                        && r1.getOutput().getCount() % r2.getInputAmount() == 0
                        && r2.getOutput().getMaxStackSize() >= r2.getOutput().getCount() * (r1.getOutput().getCount() / r2.getInputAmount())) {
                    ItemStack output = r2.getOutput().copy();
                    output.setCount(output.getCount() * (r1.getOutput().getCount() / r2.getInputAmount()));
                    ResourceLocation id = RL(r1.getId().getPath() + "_" + r2.getId().getPath());
                    drillLaser.put(id, new AssemblyRecipeImpl(id, r1.getInput(), output, AssemblyProgramType.DRILL_LASER));
                }
            }
        }
        return drillLaser;
    }

    public static class Serializer<T extends AssemblyRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.deserialize(json.get("input"));
            ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            String program = JSONUtils.getString(json, "program").toUpperCase();
            try {
                AssemblyProgramType programType = AssemblyProgramType.valueOf(program);
                Validate.isTrue(programType != AssemblyProgramType.DRILL_LASER, "'drill_laser' may not be used in recipe JSON!");
                return factory.create(recipeId, input, result, programType);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(e.getMessage());
            }
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient input = Ingredient.read(buffer);
            ItemStack out = buffer.readItemStack();
            AssemblyProgramType program = AssemblyProgramType.values()[buffer.readVarInt()];
            return factory.create(recipeId, input, out, program);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends AssemblyRecipe> {
            T create(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output, AssemblyProgramType program);
        }
    }
}
