package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IFilteringItem;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.ClassifyFilterScreen;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ClassifyFilterItem extends Item implements IFilteringItem {
    private static final String NBT_FILTER = "Filter";
    private static final String NBT_CONDITIONS = "Conditions";
    private static final String NBT_MATCH_ALL = "MatchAll";

    public ClassifyFilterItem() {
        super(ModItems.defaultProps());
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

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
        Container c = new SimpleContainer(1);
        c.setItem(0, stack);
        // TODO cache this
        return level.getRecipeManager().getRecipeFor(type, c, level).isPresent();
    }

    public static MutableComponent xlateMatch(boolean matchAll) {
        return xlate(matchAll ? "pneumaticcraft.gui.tooltip.filter.matchAll" : "pneumaticcraft.gui.tooltip.filter.matchAny");
    }

    public record FilterSettings(boolean matchAll, Collection<FilterCondition> filterConditions) implements Predicate<ItemStack> {
        private static final FilterSettings NONE = new FilterSettings(false, Collections.emptyList());

        public static FilterSettings fromStack(ItemStack filterStack) {
            CompoundTag tag = filterStack.getTagElement(NBT_FILTER);
            if (tag == null) return FilterSettings.NONE;
            ListTag l = tag.getList(NBT_CONDITIONS, Tag.TAG_STRING);
            ImmutableList.Builder<FilterCondition> builder = ImmutableList.builder();
            for (int i = 0; i < l.size(); i++) {
                try {
                    builder.add(FilterCondition.valueOf(l.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return new FilterSettings(tag.getBoolean(NBT_MATCH_ALL), builder.build());
        }

        public void save(ItemStack stack) {
            CompoundTag subTag = new CompoundTag();
            subTag.putBoolean(NBT_MATCH_ALL, matchAll);
            ListTag l = new ListTag();
            filterConditions.forEach(c -> l.add(StringTag.valueOf(c.toString())));
            subTag.put(NBT_CONDITIONS, l);
            stack.getOrCreateTag().put(NBT_FILTER, subTag);
        }

        @Override
        public boolean test(ItemStack stack) {
            return matchAll ?
                    filterConditions.stream().allMatch(c -> c.test(stack)) :
                    filterConditions.stream().anyMatch(c -> c.test(stack));
        }
    }

    public enum FilterCondition implements ITranslatableEnum, Predicate<ItemStack> {
        FUEL_ITEM(Items.COAL, s -> s.getBurnTime(RecipeType.SMELTING) > 0),
        EDIBLE(Items.BREAD, ItemStack::isEdible),
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
    }
}
