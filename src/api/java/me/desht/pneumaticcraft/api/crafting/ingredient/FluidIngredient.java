/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A fluid ingredient matcher, with fluid tag support.  Can also match items; it checks if the item contains the
 * desired fluid.
 */
public class FluidIngredient extends Ingredient {
    private List<Fluid> fluids;
    private ItemStack[] cachedStacks;
    private final int amount;
    private final ResourceLocation fluidId;
    private final TagKey<Fluid> fluidTagKey;
    private final CompoundTag nbt;
    private final boolean fuzzyNBT;

    public static final FluidIngredient EMPTY = new FluidIngredient(Collections.emptyList(), 0, null, null, null, false);

    protected FluidIngredient(List<Fluid> fluids, int amount, ResourceLocation fluidId, TagKey<Fluid> fluidTagKey, CompoundTag nbt, boolean fuzzyNBT) {
        super(Stream.empty());
        this.fluids = fluids;
        this.amount = amount;
        this.fluidId = fluidId;
        this.fluidTagKey = fluidTagKey;
        this.nbt = nbt;
        this.fuzzyNBT = fuzzyNBT;
    }

    /**
     * Create a fluid ingredient from the given FluidStack. If the FluidStack has any NBT, this will also be required
     * as an ingredient match, in a non-fuzzy way (exact match of all fields required).
     * @param fluidStack the fluidstack to match against
     * @return the fluid ingredient
     */
    public static FluidIngredient of(FluidStack fluidStack) {
        return of(fluidStack.getAmount(), fluidStack.getTag(), false, fluidStack.getFluid());
    }

    /**
     * Create a fluid ingredient from the given list of fluids
     * @param amount the amount, in mB
     * @param nbt the NBT, or null for no NBT matching
     * @param fuzzyNBT true for a fuzzy match (only fields in the ingredient will be matched), false for an exact match.
     *                Ignored if the nbt parameter is null.
     * @param fluids the list of fluids
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, @Nullable CompoundTag nbt, boolean fuzzyNBT, Fluid... fluids) {
        return new FluidIngredient(Arrays.asList(fluids), amount, null, null, nbt, fuzzyNBT);
    }

    /**
     * Create a fluid ingredient from the given list of fluids
     * @param amount the amount, in mB
     * @param fluids the list of fluids
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, Fluid... fluids) {
        return of(amount, null, false, fluids);
    }

    /**
     * Create a fluid ingredient from the given fluid tag
     * @param amount the amount, in mB
     * @param nbt the NBT, or null for no NBT matching
     * @param fuzzyNBT true for a fuzzy match (only fields in the ingredient will be matched), false for an exact match.
     *                Ignored if the nbt parameter is null.
     * @param fluidTag the fluid tag
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, @Nullable CompoundTag nbt, boolean fuzzyNBT, TagKey<Fluid> fluidTag) {
        return new FluidIngredient(null, amount, null, fluidTag, nbt, fuzzyNBT);
    }

    /**
     * Create a fluid ingredient from the given fluid tag
     * @param amount the amount, in mB
     * @param fluidTag the fluid tag
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, TagKey<Fluid> fluidTag) {
        return of(amount, null, false, fluidTag);
    }

    /**
     * Create a fluid ingredient from the given fluid registry ID. Use this if the fluid might not exist at runtime
     * (e.g. it's from another mod which may or may not be loaded). If the fluid does not exist at runtime, this
     * ingredient will never match anything.
     *
     * @param amount the amount, in mB
     * @param nbt the NBT, or null for no NBT matching
     * @param fuzzyNBT true for a fuzzy match (only fields in the ingredient will be matched), false for an exact match.
     *                Ignored if the nbt parameter is null.
     * @param fluidId the fluid's registry ID
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, @Nullable CompoundTag nbt, boolean fuzzyNBT, ResourceLocation fluidId) {
        return new FluidIngredient(null, amount, fluidId, null, nbt, fuzzyNBT);
    }

    /**
     * Create a fluid ingredient from the given fluid registry ID. Use this if the fluid might not exist at runtime
     * (e.g. it's from another mod which may or may not be loaded). If the fluid does not exist at runtime, this
     * ingredient will never match anything.
     *
     * @param amount the amount, in mB
     * @param fluidId the fluid's registry ID
     * @return the fluid ingredient
     */
    public static FluidIngredient of(int amount, ResourceLocation fluidId) {
        return of(amount, null, false, fluidId);
    }

    /**
     * Create a fluid ingredient from the given stream of other fluid ingredients. This new compound ingredient is
     * effectively a logical OR of all the constituent ingredients.
     * @param stream a stream of ingredients
     * @return the fluid ingredient
     * @apiNote this method is called "ofFluidStream" rather than "of" to avoid confusion with {@link Ingredient#of(Stream)}
     */
    public static FluidIngredient ofFluidStream(Stream<FluidIngredient> stream) {
        return new CompoundFluidIngredient(stream);
    }

    protected Collection<Fluid> getFluidList() {
        if (fluids == null) {
            if (fluidId != null) {
                if (ForgeRegistries.FLUIDS.containsKey(fluidId)) {
                    fluids = Collections.singletonList(ForgeRegistries.FLUIDS.getValue(fluidId));
                } else {
                    fluids = Collections.emptyList();
                }
            } else if (fluidTagKey != null) {
                ImmutableList.Builder<Fluid> builder = ImmutableList.builder();
                Objects.requireNonNull(ForgeRegistries.FLUIDS.tags()).getTag(fluidTagKey).forEach(builder::add);
                fluids = builder.build();
            } else {
                throw new IllegalStateException("no fluid ID or fluid tag is available?");
            }
        }
        return fluids;
    }

    @Override
    public boolean isEmpty() {
        return getFluidList().isEmpty() || getFluidList().stream().allMatch(f -> f == Fluids.EMPTY);
    }

    /**
     * Test the given item against this ingredient. The item must be a fluid container item (providing the
     * {@link ForgeCapabilities#FLUID_HANDLER_ITEM} capability) containing fluid which matches
     * this ingredient, AND it must be a container item
     * ({@link net.minecraftforge.common.extensions.IForgeItem#hasCraftingRemainingItem(ItemStack)} must return true).
     *
     * @param stack the itemstack to test
     * @return true if the fluid in the given itemstack matches this ingredient
     */
    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && stack.hasCraftingRemainingItem() && FluidUtil.getFluidContained(stack).map(this::testFluid).orElse(false);
    }

    @Override
    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            List<ItemStack> tankList = new ArrayList<>();
            for (Fluid f : getFluidList()) {
                FluidStack fluidStack = new FluidStack(f, 1000);
                ItemStack bucket = FluidUtil.getFilledBucket(fluidStack);
                if (!bucket.isEmpty()) tankList.add(bucket);
                Stream.of("small", "medium", "large", "huge")
                        .map(tankName -> ForgeRegistries.BLOCKS.getValue(PneumaticRegistry.RL(tankName + "_tank")))
                        .filter(tankBlock -> tankBlock != null && tankBlock != Blocks.AIR)
                        .forEach(tankBlock -> maybeAddTank(tankList, tankBlock, fluidStack));
            }
            cachedStacks = tankList.toArray(new ItemStack[0]);
        }
        return cachedStacks;
    }

    /**
     * Test the given fluid stack against this ingredient. The fluid must match, and the fluid stack amount must be at
     * least as large. In addition, if the ingredient specifies any NBT, that must also match.
     *
     * @param fluidStack the fluid stack to test
     * @return true if the fluid stack matches, false otherwise
     */
    public boolean testFluid(FluidStack fluidStack) {
        return getFluidList().stream().anyMatch(f ->
                fluidStack.getFluid() == f &&
                        fluidStack.getAmount() >= getAmount() &&
                        matchNBT(fluidStack)
        );
    }

    /**
     * Test the given fluid against this ingredient. Just a fluid match; no amount or NBT matching is done.
     *
     * @param fluid the fluid to test
     * @return true if the fluid matches, false otherwise
     */
    public boolean testFluid(Fluid fluid) {
        return getFluidList().stream().anyMatch(f -> f == fluid);
    }

    private void maybeAddTank(List<ItemStack> l, Block tankBlock, FluidStack stack) {
        ItemStack tank = new ItemStack(tankBlock);
        tank.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(h -> {
            h.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            l.add(h.getContainer());
        });
    }

    private boolean matchNBT(FluidStack fluidStack) {
        if (nbt == null) return true;  // null means "don't care" in this context
        if (fluidStack.getTag() == null) return false;

        if (fuzzyNBT) {
            // match only the fields which are actually present in the ingredient
            return nbt.getAllKeys().stream().allMatch(key -> NbtUtils.compareNbt(nbt.get(key), fluidStack.getTag().get(key), true));
        } else {
            // exact match of all fields is required
            return NbtUtils.compareNbt(nbt, fluidStack.getTag(), true);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        if (fluidTagKey != null) {
            json.addProperty("tag", fluidTagKey.location().toString());
        } else if (fluidId != null) {
            json.addProperty("fluid", fluidId.toString());
        } else if (!fluids.isEmpty()) {
            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluids.get(0));
            if (rl != null)  json.addProperty("fluid", rl.toString());
        } else {
            throw new IllegalStateException("ingredient has no ID, tag or fluid!");
        }
        json.addProperty("amount", getAmount());
        if (nbt != null) {
            json.addProperty("nbt", nbt.toString());
            json.addProperty("fuzzyNBT", fuzzyNBT);
        }
        return json;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public int getAmount() {
        return amount;
    }

    public List<FluidStack> getFluidStacks() {
        return getFluidList().stream().map(f -> new FluidStack(f, getAmount())).collect(Collectors.toList());
    }

    public static class Serializer implements IIngredientSerializer<FluidIngredient> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:fluid");
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        @Override
        public FluidIngredient parse(FriendlyByteBuf buffer) {
            int nFluids = buffer.readVarInt();
            int amount = buffer.readVarInt();
            Set<Fluid> fluids = new HashSet<>();
            for (int i = 0; i < nFluids; i++) {
                fluids.add(buffer.readRegistryIdSafe(Fluid.class));
            }
            if (buffer.readBoolean()) {
                return FluidIngredient.of(amount, buffer.readNbt(), buffer.readBoolean(), fluids.toArray(new Fluid[0]));
            } else {
                return FluidIngredient.of(amount, fluids.toArray(new Fluid[0]));
            }
        }

        @Override
        public FluidIngredient parse(JsonObject json) {
            int amount = GsonHelper.getAsInt(json, "amount", 1000);
            FluidIngredient result;
            CompoundTag nbt = possibleNBT(json);
            boolean fuzzyNBT = GsonHelper.getAsBoolean(json, "fuzzyNBT", false);
            if (json.has("tag")) {
                ResourceLocation rl = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                result = FluidIngredient.of(amount, nbt, fuzzyNBT, TagKey.create(Registries.FLUID, rl));
            } else if (json.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("Unknown fluid '" + fluidId + "'");
                result = FluidIngredient.of(amount, nbt, fuzzyNBT, fluid);
            } else {
                throw new JsonSyntaxException("fluid ingredient must have 'fluid' or 'tag' field!");
            }
            return result;
        }

        private CompoundTag possibleNBT(JsonObject json) {
            if (json.has("nbt")) {
                JsonElement element = json.get("nbt");
                try {
                    if (element.isJsonObject())
                        return TagParser.parseTag(GSON.toJson(element));
                    else
                        return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException(e);
                }
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buffer, FluidIngredient ingredient) {
            buffer.writeVarInt(ingredient.getFluidList().size());
            buffer.writeVarInt(ingredient.getAmount());
            ingredient.getFluidList().forEach(fluid -> buffer.writeRegistryId(ForgeRegistries.FLUIDS, fluid));
            if (ingredient.nbt != null) {
                buffer.writeBoolean(true);
                buffer.writeNbt(ingredient.nbt);
                buffer.writeBoolean(ingredient.fuzzyNBT);
            } else {
                buffer.writeBoolean(false);
            }
        }
    }
}
