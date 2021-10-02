package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
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
    private final ITag<Fluid> fluidTag;
    private final CompoundNBT nbt;
    private final boolean fuzzyNBT;

    public static final FluidIngredient EMPTY = new FluidIngredient(Collections.emptyList(), 0, null, null, null, false);

    protected FluidIngredient(List<Fluid> fluids, int amount, ResourceLocation fluidId, ITag<Fluid> fluidTag, CompoundNBT nbt, boolean fuzzyNBT) {
        super(Stream.empty());
        this.fluids = fluids;
        this.amount = amount;
        this.fluidId = fluidId;
        this.fluidTag = fluidTag;
        this.nbt = nbt;
        this.fuzzyNBT = fuzzyNBT;
    }

    public static FluidIngredient of(FluidStack fluidStack) {
        return of(fluidStack.getAmount(), fluidStack.getTag(), false, fluidStack.getFluid());
    }

    public static FluidIngredient of(int amount, CompoundNBT nbt, boolean fuzzyNBT, Fluid... fluids) {
        return new FluidIngredient(Arrays.asList(fluids), amount, null, null, nbt, fuzzyNBT);
    }

    public static FluidIngredient of(int amount, Fluid... fluids) {
        return of(amount, null, false, fluids);
    }

    public static FluidIngredient of(int amount, CompoundNBT nbt, boolean fuzzyNBT, ITag<Fluid> fluidTag) {
        return new FluidIngredient(null, amount, null, fluidTag, nbt, fuzzyNBT);
    }

    public static FluidIngredient of(int amount, ITag<Fluid> fluidTag) {
        return of(amount, null, false, fluidTag);
    }

    public static FluidIngredient of(int amount, CompoundNBT nbt, boolean fuzzyNBT, ResourceLocation fluidId) {
        return new FluidIngredient(null, amount, fluidId, null, nbt, fuzzyNBT);
    }

    public static FluidIngredient of(int amount, ResourceLocation fluidId) {
        return of(amount, null, false, fluidId);
    }

    // not called "of" due to clash with Ingredient.of
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
            } else if (fluidTag != null) {
                fluids = ImmutableList.copyOf(fluidTag.getValues());
            } else {
                throw new IllegalStateException("no fluid ID or fluid tag is available?");
            }
        }
        return fluids;
    }

    @Override
    public boolean isEmpty() {
        return getFluidList().isEmpty();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        // an item can be used as fluid ingredient (e.g. speed upgrade crafting using bucket or tank of lubricant) iff
        // 1. it provides a fluid handler capability, AND
        // 2. it acts a container item so it doesn't just get voided when crafted with
        return stack != null && stack.hasContainerItem() && FluidUtil.getFluidContained(stack).map(this::testFluid).orElse(false);
    }

    @Override
    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            List<ItemStack> tankList = new ArrayList<>();
            for (Fluid f : getFluidList()) {
                FluidStack fluidStack = new FluidStack(f, 1000);
                ItemStack bucket = FluidUtil.getFilledBucket(fluidStack);
                if (!bucket.isEmpty()) tankList.add(bucket);
                for (String tankName : new String[] { "small", "medium", "large", "huge" }) {
                    Block tankBlock = ForgeRegistries.BLOCKS.getValue(PneumaticRegistry.RL(tankName + "_tank"));
                    if (tankBlock != null && tankBlock != Blocks.AIR) {
                        maybeAddTank(tankList, tankBlock, fluidStack);
                    }
                }
            }
            cachedStacks = tankList.toArray(new ItemStack[0]);
        }
        return cachedStacks;
    }

    private void maybeAddTank(List<ItemStack> l, Block tankBlock, FluidStack stack) {
        ItemStack tank = new ItemStack(tankBlock);
        tank.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
            h.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            l.add(h.getContainer());
        });
    }

    public boolean testFluid(FluidStack fluidStack) {
        return getFluidList().stream().anyMatch(f ->
                fluidStack.getFluid() == f &&
                fluidStack.getAmount() >= getAmount() &&
                matchNBT(fluidStack)
        );
    }

    private boolean matchNBT(FluidStack fluidStack) {
        if (nbt == null) return true;  // null means "don't care" in this context
        if (fluidStack.getTag() == null) return false;

        if (fuzzyNBT) {
            // match only the fields which are actually present in the ingredient
            return nbt.getAllKeys().stream().allMatch(key -> NBTUtil.compareNbt(nbt.get(key), fluidStack.getTag().get(key), true));
        } else {
            return NBTUtil.compareNbt(nbt, fluidStack.getTag(), true);
        }
    }

    public boolean testFluid(Fluid otherFluid) {
        return getFluidList().stream().anyMatch(f -> f == otherFluid);
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        if (fluidTag != null) {
            ResourceLocation tagId = TagCollectionManager.getInstance().getFluids().getIdOrThrow(fluidTag);
            json.addProperty("tag", tagId.toString());
        } else if (fluidId != null) {
            json.addProperty("fluid", fluidId.toString());
        } else if (!fluids.isEmpty()) {
            json.addProperty("fluid", fluids.get(0).getRegistryName().toString());
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
        public FluidIngredient parse(PacketBuffer buffer) {
            int nFluids = buffer.readVarInt();
            int amount = buffer.readVarInt();
            Set<Fluid> fluids = new HashSet<>();
            for (int i = 0; i < nFluids; i++) {
                fluids.add(buffer.readRegistryId());
            }
            if (buffer.readBoolean()) {
                return FluidIngredient.of(amount, buffer.readNbt(), buffer.readBoolean(), fluids.toArray(new Fluid[0]));
            } else {
                return FluidIngredient.of(amount, fluids.toArray(new Fluid[0]));
            }
        }

        @Override
        public FluidIngredient parse(JsonObject json) {
            int amount = JSONUtils.getAsInt(json, "amount", 1000);
            FluidIngredient result;
            CompoundNBT nbt = possibleNBT(json);
            boolean fuzzyNBT = JSONUtils.getAsBoolean(json, "fuzzyNBT", false);
            if (json.has("tag")) {
                ResourceLocation rl = new ResourceLocation(JSONUtils.getAsString(json, "tag"));
                ITag<Fluid> tag = TagCollectionManager.getInstance().getFluids().getTag(rl);
                if (tag == null) throw new JsonSyntaxException("Unknown fluid tag '" + rl + "'");
                result = FluidIngredient.of(amount, nbt, fuzzyNBT, tag);
            } else if (json.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(JSONUtils.getAsString(json, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("Unknown fluid '" + fluidId + "'");
                result = FluidIngredient.of(amount, nbt, fuzzyNBT, fluid);
            } else {
                throw new JsonSyntaxException("fluid ingredient must have 'fluid' or 'tag' field!");
            }
            return result;
        }

        private CompoundNBT possibleNBT(JsonObject json) {
            if (json.has("nbt")) {
                JsonElement element = json.get("nbt");
                try {
                    if (element.isJsonObject())
                        return JsonToNBT.parseTag(GSON.toJson(element));
                    else
                        return JsonToNBT.parseTag(JSONUtils.convertToString(element, "nbt"));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException(e);
                }
            }
            return null;
        }

        @Override
        public void write(PacketBuffer buffer, FluidIngredient ingredient) {
            buffer.writeVarInt(ingredient.getFluidList().size());
            buffer.writeVarInt(ingredient.getAmount());
            ingredient.getFluidList().forEach(buffer::writeRegistryId);
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
