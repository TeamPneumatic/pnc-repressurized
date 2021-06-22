package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
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
    private final int amount;
    private ItemStack[] cachedStacks;
    private final ResourceLocation fluidId;
    private final ITag<Fluid> fluidTag;

    public static final FluidIngredient EMPTY = new FluidIngredient(Collections.emptyList(), 0, null, null);

    protected FluidIngredient(List<Fluid> fluids, int amount, ResourceLocation fluidId, ITag<Fluid> fluidTag) {
        super(Stream.empty());
        this.fluids = fluids;
        this.amount = amount;
        this.fluidId = fluidId;
        this.fluidTag = fluidTag;
    }

    public static FluidIngredient of(FluidStack fluidStack) {
        return of(fluidStack.getAmount(), fluidStack.getFluid());
    }

    public static FluidIngredient of(int amount, Fluid... fluids) {
        return new FluidIngredient(Arrays.asList(fluids), amount, null, null);
    }

    public static FluidIngredient of(int amount, ITag<Fluid> fluidTag) {
        return new FluidIngredient(null, amount, null, fluidTag);
    }

    public static FluidIngredient of(int amount, ResourceLocation fluidId) {
        return new FluidIngredient(null, amount, fluidId, null);
    }

    public static FluidIngredient of(Stream<FluidIngredient> stream) {
        return new CompoundFluidIngredient(stream);
    }

    protected Collection<Fluid> getFluidList() {
        if (fluids == null) {
            if (fluidId != null) {
                Fluid f = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (f != null && f != Fluids.EMPTY) {
                    fluids = Collections.singletonList(f);
                }
            } else if (fluidTag != null) {
                fluids = ImmutableList.copyOf(fluidTag.getAllElements());
            } else {
                throw new IllegalStateException("no fluid ID or fluid tag is available?");
            }
        }
        return fluids;
    }

    @Override
    public boolean hasNoMatchingItems() {
        return getFluidList().isEmpty();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && FluidUtil.getFluidContained(stack).map(this::testFluid).orElse(false);
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        if (cachedStacks == null) {
            List<ItemStack> l = new ArrayList<>();
            for (Fluid f : getFluidList()) {
                FluidStack fluidStack = new FluidStack(f, 1000);
                ItemStack bucket = FluidUtil.getFilledBucket(fluidStack);
                if (!bucket.isEmpty()) l.add(bucket);
                maybeAddTank(l, ModBlocks.TANK_SMALL.get(), fluidStack);
                maybeAddTank(l, ModBlocks.TANK_MEDIUM.get(), fluidStack);
                maybeAddTank(l, ModBlocks.TANK_LARGE.get(), fluidStack);
                maybeAddTank(l, ModBlocks.TANK_HUGE.get(), fluidStack);
            }
            cachedStacks = l.toArray(new ItemStack[0]);
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
        return getFluidList().stream().anyMatch(f -> fluidStack.getFluid() == f && fluidStack.getAmount() >= getAmount());
    }

    public boolean testFluid(Fluid otherFluid) {
        return getFluidList().stream().anyMatch(f -> f == otherFluid);
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        if (fluidTag != null) {
            ResourceLocation tagId = TagCollectionManager.getManager().getFluidTags().getValidatedIdFromTag(fluidTag);
            json.addProperty("tag", tagId.toString());
        } else if (fluidId != null) {
            json.addProperty("fluid", fluidId.toString());
        } else if (!fluids.isEmpty()) {
            json.addProperty("fluid", fluids.get(0).getRegistryName().toString());
        } else {
            throw new IllegalStateException("ingredient has no ID, tag or fluid!");
        }
        json.addProperty("amount", getAmount());
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

        @Override
        public FluidIngredient parse(PacketBuffer buffer) {
            int n = buffer.readVarInt();
            int amount = buffer.readVarInt();
            Set<Fluid> fluids = new HashSet<>();
            for (int i = 0; i < n; i++) {
                fluids.add(buffer.readRegistryId());
            }
            return FluidIngredient.of(amount, fluids.toArray(new Fluid[0]));
        }

        @Override
        public FluidIngredient parse(JsonObject json) {
            int amount = JSONUtils.getInt(json, "amount", 1000);
            if (json.has("tag")) {
                ResourceLocation rl = new ResourceLocation(JSONUtils.getString(json, "tag"));
                ITag<Fluid> tag = TagCollectionManager.getManager().getFluidTags().get(rl);
                if (tag == null) throw new JsonSyntaxException("Unknown fluid tag '" + rl + "'");
                return FluidIngredient.of(amount, tag);
            } else if (json.has("fluid")) {
                ResourceLocation fluidName = new ResourceLocation(JSONUtils.getString(json, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
                if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("Unknown fluid '" + fluidName + "'");
                return FluidIngredient.of(amount, fluid);
            } else {
                throw new JsonSyntaxException("fluid ingredient must have 'fluid' or 'tag' field!");
            }
        }

        @Override
        public void write(PacketBuffer buffer, FluidIngredient ingredient) {
            buffer.writeVarInt(ingredient.getFluidList().size());
            buffer.writeVarInt(ingredient.getAmount());
            for (Fluid fluid : ingredient.getFluidList()) {
                buffer.writeRegistryId(fluid);
            }
        }
    }
}
