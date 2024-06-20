package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.base.Suppliers;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An ingredient which matches items that contain at least the given amount of fluid. Items must support
 * the {@code Capabilities.FluidHandler.ITEM} item capability.
 *
 * @param either the fluidstack OR fluid-tag/amount which must be contained in matching items
 */
public record FluidContainerIngredient(Either<FluidStack,TagWithAmount> either) implements ICustomIngredient {
    public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.either(FluidStack.CODEC, TagWithAmount.CODEC).fieldOf("fluid").forGetter(FluidContainerIngredient::either)
    ).apply(builder, FluidContainerIngredient::new));

    private static final Supplier<List<Block>> TANK_BLOCKS = Suppliers.memoize(() ->
            Stream.of(
                    PneumaticRegistry.RL("small_tank"),
                    PneumaticRegistry.RL("medium_tank"),
                    PneumaticRegistry.RL("large_tank"),
                    PneumaticRegistry.RL("huge_tank")
            ).map(BuiltInRegistries.BLOCK::get).toList());

    public static FluidContainerIngredient of(Fluid fluid, int amount) {
        return new FluidContainerIngredient(Either.left(new FluidStack(fluid, amount)));
    }

    public static FluidContainerIngredient of(TagKey<Fluid> tag, int amount) {
        return new FluidContainerIngredient(Either.right(new TagWithAmount(tag, amount)));
    }

    @Override
    public boolean test(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        return either.map(
                fluidStack -> handler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE).getAmount() >= fluidStack.getAmount(),
                tagWithAmount -> {
                    FluidStack contained = handler.drain(tagWithAmount.amount, IFluidHandler.FluidAction.SIMULATE);
                    return contained.getFluid().is(tagWithAmount.tag) && contained.getAmount() == tagWithAmount.amount;
                }
        );
    }

    @Override
    public Stream<ItemStack> getItems() {
        int amount = either.map(FluidStack::getAmount, TagWithAmount::amount);
        List<FluidStack> fluidStacks = either.map(
                List::of,
                tagWithAmount -> StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tagWithAmount.tag).spliterator(), false)
                        .map(holder -> new FluidStack(holder.value(), amount))
                        .toList()
        );

        Stream<ItemStack> buckets = amount == FluidType.BUCKET_VOLUME ?
                fluidStacks.stream().map(FluidUtil::getFilledBucket) :
                Stream.empty();

        IItemRegistry reg = PneumaticRegistry.getInstance().getItemRegistry();
        Stream<ItemStack> tanks = fluidStacks.stream().flatMap(fluidStack ->
                TANK_BLOCKS.get().stream().map(b -> reg.createFluidContainingItem(b, fluidStack))
        );

        return Streams.concat(buckets, tanks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PneumaticRegistry.getInstance().getCustomIngredientTypes().fluidContainerType().get();
    }

    public record TagWithAmount(TagKey<Fluid> tag, int amount) {
        public static final Codec<TagWithAmount> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(TagWithAmount::tag),
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(TagWithAmount::amount)
        ).apply(builder, TagWithAmount::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TagWithAmount> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location), TagWithAmount::tag,
                ByteBufCodecs.INT, TagWithAmount::amount,
                TagWithAmount::new
        );
    }
}
