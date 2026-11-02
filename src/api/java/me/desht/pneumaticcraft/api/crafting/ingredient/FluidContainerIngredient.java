package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.base.Suppliers;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An ingredient which matches items that contain at least the given amount of fluid. Items must support
 * the {@code Capabilities.Fluid.ITEM} item capability.
 *
 * @param either the fluidstack OR fluid-tag/amount which must be contained in matching items
 */
public record FluidContainerIngredient(Either<FluidStack,TagWithAmount> either) implements ICustomIngredient {
    public static final MapCodec<FluidContainerIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.either(FluidStack.CODEC, TagWithAmount.CODEC).fieldOf("fluid").forGetter(FluidContainerIngredient::either)
    ).apply(builder, FluidContainerIngredient::new));
    public static final Codec<FluidContainerIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.either(FluidStack.CODEC, TagWithAmount.CODEC).fieldOf("fluid").forGetter(FluidContainerIngredient::either)
    ).apply(builder, FluidContainerIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.either(FluidStack.STREAM_CODEC, TagWithAmount.STREAM_CODEC), FluidContainerIngredient::either,
            FluidContainerIngredient::new
    );

    private static final Supplier<List<Block>> TANK_BLOCKS = Suppliers.memoize(() ->
            Stream.of(
                    PneumaticRegistry.RL("small_tank"),
                    PneumaticRegistry.RL("medium_tank"),
                    PneumaticRegistry.RL("large_tank"),
                    PneumaticRegistry.RL("huge_tank")
            ).map(BuiltInRegistries.BLOCK::getValue).toList());

    public static FluidContainerIngredient of(Fluid fluid, int amount) {
        return new FluidContainerIngredient(Either.left(new FluidStack(fluid, amount)));
    }

    public static FluidContainerIngredient of(TagKey<Fluid> tag, int amount) {
        return new FluidContainerIngredient(Either.right(new TagWithAmount(tag, amount)));
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty() || stack.getCraftingRemainder() == null) return false;

        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copy()).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return false;
        try (Transaction tx = Transaction.openRoot()) {
            return either.map(
                    fluidStack -> handler.extract(FluidResource.of(fluidStack), fluidStack.getAmount(), tx) >= fluidStack.getAmount(),
                    tagWithAmount -> {
                        FluidResource contained = ResourceHandlerUtil.findExtractableResource(handler, r -> r.is(tagWithAmount.tag), tx);
                        return contained != null && handler.extract(contained, tagWithAmount.amount, tx) == tagWithAmount.amount;
                    }
            );
        }
    }

    @Override
    public Stream<Holder<Item>> items() {
        return filledStacks().map(ItemStack::typeHolder).distinct();
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(filledStacks()
                .<SlotDisplay>map(stack -> new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
                .toList());
    }

    private Stream<ItemStack> filledStacks() {
        int amount = either.map(FluidStack::getAmount, TagWithAmount::amount);
        List<FluidStack> fluidStacks = either.map(
                List::of,
                tagWithAmount -> StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tagWithAmount.tag).spliterator(), false)
                        .map(holder -> new FluidStack(holder.value(), amount))
                        .toList()
        );

        Stream<ItemStack> buckets = amount == FluidType.BUCKET_VOLUME ?
                fluidStacks.stream().map(fs -> new ItemStack(fs.getFluid().getBucket())) :
                Stream.empty();

        IItemRegistry reg = PneumaticRegistry.getInstance().getItemRegistry();
        Stream<ItemStack> tanks = fluidStacks.stream().flatMap(fluidStack ->
                TANK_BLOCKS.get().stream().map(b -> reg.createFluidContainingItem(b, fluidStack))
        );

        return Streams.concat(buckets, tanks).filter(stack -> !stack.isEmpty());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PneumaticRegistry.getInstance().getCustomIngredientTypes().fluidContainerType().get();
    }

    public int amount() {
        return either.map(FluidStack::getAmount, TagWithAmount::amount);
    }

    public record TagWithAmount(TagKey<Fluid> tag, int amount) {
        public static final Codec<TagWithAmount> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(TagWithAmount::tag),
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(TagWithAmount::amount)
        ).apply(builder, TagWithAmount::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TagWithAmount> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location), TagWithAmount::tag,
                ByteBufCodecs.INT, TagWithAmount::amount,
                TagWithAmount::new
        );
    }
}
