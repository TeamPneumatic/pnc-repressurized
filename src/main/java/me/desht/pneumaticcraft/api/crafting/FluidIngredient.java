package me.desht.pneumaticcraft.api.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A fluid ingredient matcher, with fluid tag support.  Can also match items; it checks if the item contains the
 * desired fluid.
 */
public class FluidIngredient extends Ingredient {
    private final List<Fluid> fluids;
    private final int amount;
    private ItemStack[] cachedStacks;
    private final ResourceLocation tagId;

    public static final FluidIngredient EMPTY = new FluidIngredient();

    public FluidIngredient(Fluid fluid, int amount) {
        this(new FluidStack(fluid, amount));
    }

    private FluidIngredient() {
        super(Stream.empty());
        this.fluids = Collections.emptyList();
        this.amount = 0;
        this.tagId = null;
    }

    private FluidIngredient(FluidStack fluidStack) {
        super(Stream.empty());
        Validate.isTrue(!fluidStack.isEmpty());
        this.fluids = Collections.singletonList(fluidStack.getFluid());
        this.amount = fluidStack.getAmount();
        this.tagId = null;
    }

    private FluidIngredient(Tag<Fluid> tag, int amount) {
        super(Stream.empty());
        this.fluids = ImmutableList.copyOf(tag.getAllElements());
        this.amount = amount;
        this.tagId = tag.getId();
    }

    @Override
    public boolean hasNoMatchingItems() {
        return fluids.isEmpty();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(this::testFluid).orElse(false);
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        if (cachedStacks == null) {
            cachedStacks = fluids.stream()
                    .map(fluid -> FluidUtil.getFilledBucket(new FluidStack(fluid, 1000)))
                    .toArray(ItemStack[]::new);
        }
        return cachedStacks;
    }

    public boolean testFluid(FluidStack fluidStack) {
        return fluids.stream().anyMatch(f -> fluidStack.getFluid() == f && fluidStack.getAmount() >= amount);
    }

    public boolean testFluid(Fluid otherFluid) {
        return fluids.stream().anyMatch(f -> f == otherFluid);
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        if (tagId == null) {
            json.addProperty("fluid", fluids.get(0).getRegistryName().toString());
        } else {
            json.addProperty("tag", tagId.toString());
        }
        json.addProperty("amount", amount);
        return json;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public void writeToPacket(PacketBuffer buffer) {
        Serializer.INSTANCE.write(buffer, this);
    }

    public static FluidIngredient readFromPacket(PacketBuffer buffer) {
        return Serializer.INSTANCE.parse(buffer);
    }

    public int getAmount() {
        return amount;
    }

    public List<FluidStack> getFluidStacks() {
        return fluids.stream().map(f -> new FluidStack(f, amount)).collect(Collectors.toList());
    }

    public static class Serializer implements IIngredientSerializer<FluidIngredient> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:fluid");

        @Override
        public FluidIngredient parse(PacketBuffer buffer) {
            boolean isTag = buffer.readBoolean();
            if (isTag) {
                ResourceLocation tagId = buffer.readResourceLocation();
                int amount = buffer.readInt();
                Tag<Fluid> tag = FluidTags.getCollection().get(tagId);
                if (tag == null) throw new JsonSyntaxException("Failed to read fluid tag from buffer");
                return new FluidIngredient(FluidTags.getCollection().get(tagId), amount);
            } else {
                FluidStack stack = buffer.readFluidStack();
                if (stack.isEmpty()) throw new JsonSyntaxException("Failed to read fluidstack from buffer");
                return new FluidIngredient(stack);
            }
        }

        @Override
        public FluidIngredient parse(JsonObject json) {
            int amount = JSONUtils.getInt(json, "amount", 1000);
            if (json.has("tag")) {
                ResourceLocation rl = new ResourceLocation(JSONUtils.getString(json, "tag"));
                Tag<Fluid> tag = FluidTags.getCollection().get(rl);
                if (tag == null) {
                    throw new JsonSyntaxException("Unknown fluid tag '" + rl + "'");
                }
                return new FluidIngredient(tag, amount);
            } else if (json.has("fluid")) {
                ResourceLocation fluidName = new ResourceLocation(JSONUtils.getString(json, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
                if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("Unknown fluid '" + fluidName + "'");
                return new FluidIngredient(fluid, amount);
            } else {
                throw new JsonSyntaxException("fluid ingredient must have 'fluid' or 'tag' field!");
            }
        }

        @Override
        public void write(PacketBuffer buffer, FluidIngredient ingredient) {
            if (ingredient.tagId == null) {
                buffer.writeBoolean(false);
                buffer.writeFluidStack(new FluidStack(ingredient.fluids.get(0), ingredient.amount));
            } else {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(ingredient.tagId);
                buffer.writeInt(ingredient.amount);
            }
        }
    }
}
