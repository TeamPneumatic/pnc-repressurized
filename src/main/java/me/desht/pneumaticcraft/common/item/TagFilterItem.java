/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.api.item.IFilteringItem;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TagFilterItem extends Item implements IFilteringItem {
    public TagFilterItem() {
        super(ModItems.defaultProps().stacksTo(1).component(ModDataComponents.TAG_FILTER_KEYS, List.of()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        if (context.registries() != null) {
            tooltip.add(xlate("pneumaticcraft.gui.tooltip.tag_filter.header").withStyle(ChatFormatting.YELLOW));
            for (TagKey<Item> key : getConfiguredTagList(stack)) {
                tooltip.add(Symbols.bullet().append(key.location().toString()).withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public static Set<TagKey<Item>> getConfiguredTagList(ItemStack stack) {
        List<ResourceLocation> tagIds = stack.get(ModDataComponents.TAG_FILTER_KEYS);
        return tagIds != null && !tagIds.isEmpty() ?
                tagIds.stream().map(r -> TagKey.create(Registries.ITEM, r)).collect(Collectors.toSet()) :
                Set.of();
    }

    public static void setConfiguredTagList(ItemStack stack, Set<TagKey<Item>> tags) {
        if (tags.isEmpty()) {
            stack.remove(ModDataComponents.TAG_FILTER_KEYS);
        } else {
            stack.set(ModDataComponents.TAG_FILTER_KEYS, tags.stream().map(TagKey::location).toList());
        }
    }

    @Override
    public boolean matchFilter(ItemStack filterStack, ItemStack stack) {
        Validate.isTrue(filterStack.getItem() instanceof TagFilterItem, "filtering itemstack is not a tag filter!");
        Set<TagKey<Item>> tags = getConfiguredTagList(filterStack);
        return !Sets.intersection(tags, PneumaticCraftUtils.itemTags(stack.getItem())).isEmpty();
    }

}
