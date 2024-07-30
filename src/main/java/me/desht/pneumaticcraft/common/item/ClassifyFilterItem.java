package me.desht.pneumaticcraft.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.item.IFilteringItem;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.ClassifyFilterScreen;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.getOptionalComponent;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ClassifyFilterItem extends Item implements IFilteringItem {

    public ClassifyFilterItem() {
        super(ModItems.defaultProps().component(ModDataComponents.CLASSIFY_FILTER_SETTINGS, FilterSettings.NONE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.isClientSide) {
            ClassifyFilterScreen.openGui(stack.getHoverName(), pUsedHand);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean matchFilter(ItemStack filterStack, ItemStack stack) {
        Validate.isTrue(filterStack.getItem() instanceof ClassifyFilterItem, "filter item stack is not a Classify Filter!");

        return FilterSettings.fromStack(filterStack).test(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);

        FilterSettings settings = FilterSettings.fromStack(pStack);
        pTooltipComponents.add(xlateMatch(settings.matchAll()).withStyle(ChatFormatting.YELLOW));
        for (FilterCondition c : settings.filterConditions()) {
            pTooltipComponents.add(Symbols.bullet().append(xlate(c.getTranslationKey())).withStyle(ChatFormatting.GOLD));
        }
    }

    private static boolean isCookable(RecipeType<? extends AbstractCookingRecipe> type, ItemStack stack) {
        Level level = ServerLifecycleHooks.getCurrentServer().overworld();
        //noinspection ConstantConditions
        if (level == null) return false;
        SingleRecipeInput input = new SingleRecipeInput(stack);
        // TODO cache this
        return level.getRecipeManager().getRecipeFor(type, input, level).isPresent();
    }

    public static MutableComponent xlateMatch(boolean matchAll) {
        return xlate(matchAll ? "pneumaticcraft.gui.tooltip.filter.matchAll" : "pneumaticcraft.gui.tooltip.filter.matchAny");
    }

    public record FilterSettings(boolean matchAll, List<FilterCondition> filterConditions) implements Predicate<ItemStack> {
        public static final FilterSettings NONE = new FilterSettings(false, Collections.emptyList());

        public static final Codec<FilterSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.BOOL.optionalFieldOf("match_all", false).forGetter(FilterSettings::matchAll),
                StringRepresentable.fromEnum(FilterCondition::values).listOf().fieldOf("conditions").forGetter(FilterSettings::filterConditions)
        ).apply(builder, FilterSettings::new));

        public static final StreamCodec<FriendlyByteBuf, FilterSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, FilterSettings::matchAll,
                NeoForgeStreamCodecs.enumCodec(FilterCondition.class).apply(ByteBufCodecs.list()), FilterSettings::filterConditions,
                FilterSettings::new
        );

        public static FilterSettings fromStack(ItemStack filterStack) {
            return filterStack.getOrDefault(ModDataComponents.CLASSIFY_FILTER_SETTINGS, FilterSettings.NONE);
        }

        public void save(ItemStack stack) {
            stack.set(ModDataComponents.CLASSIFY_FILTER_SETTINGS, this);
        }

        @Override
        public boolean test(ItemStack stack) {
            return matchAll ?
                    filterConditions.stream().allMatch(c -> c.test(stack)) :
                    filterConditions.stream().anyMatch(c -> c.test(stack));
        }
    }

    public enum FilterCondition implements ITranslatableEnum, Predicate<ItemStack>, StringRepresentable {
        FUEL_ITEM(Items.COAL, s -> s.getBurnTime(RecipeType.SMELTING) > 0),
        EDIBLE(Items.BREAD, s -> getOptionalComponent(s, DataComponents.FOOD).map(f -> f.nutrition() > 0).orElse(false)),
        PLACEABLE(Items.STONE, s -> s.getItem() instanceof BlockItem),
        FLUID_CONTAINER(Items.BUCKET, s -> IOHelper.getFluidHandlerForItem(s).isPresent()),
        UNSTACKABLE(Items.WRITABLE_BOOK, s -> s.getMaxStackSize() == 1),
        WEARABLE(Items.LEATHER_HELMET, s -> s.getItem() instanceof ArmorItem),
        TOOL(Items.IRON_PICKAXE, s -> s.getItem() instanceof TieredItem),
        WEAPON(Items.IRON_SWORD, s -> s.getItem() instanceof SwordItem || s.getItem() instanceof AxeItem || s.getItem() instanceof ProjectileWeaponItem),
        ENCHANTABLE(Items.BOOK, s -> s.isEnchantable() && !s.isEnchanted()),
        ENCHANTED(Items.ENCHANTED_BOOK, ItemStack::isEnchanted),
        SMELTABLE(Blocks.FURNACE, s -> isCookable(RecipeType.SMELTING, s)),
        BLASTABLE(Blocks.BLAST_FURNACE, s -> isCookable(RecipeType.BLASTING, s)),
        SMOKABLE(Blocks.SMOKER, s -> isCookable(RecipeType.SMOKING, s)),
        CAMPFIRE_COOKABLE(Blocks.CAMPFIRE, s -> isCookable(RecipeType.CAMPFIRE_COOKING, s));

        private final ItemStack icon;
        private final Predicate<ItemStack> predicate;

        FilterCondition(ItemLike icon, Predicate<ItemStack> predicate) {
            this.icon = new ItemStack(icon);
            this.predicate = predicate;
        }

        public ItemStack getIcon() {
            return icon;
        }

        @Override
        public boolean test(ItemStack stack) {
            return predicate.test(stack);
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tooltip.filter." + this.toString().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
