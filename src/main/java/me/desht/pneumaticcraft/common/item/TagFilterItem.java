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
import me.desht.pneumaticcraft.api.item.ITagFilteringItem;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TagFilterItem extends Item implements ITagFilteringItem {
    private static final String NBT_TAG_LIST = "TagList";

    public TagFilterItem() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn != null) {
            tooltip.add(xlate("pneumaticcraft.gui.tooltip.tag_filter.header").withStyle(ChatFormatting.YELLOW));
            for (ResourceLocation rl : getConfiguredTagList(stack)) {
                tooltip.add(Symbols.bullet().append(rl.toString()).withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public static Set<ResourceLocation> getConfiguredTagList(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(NBT_TAG_LIST)) {
            ListTag l = nbt.getList("TagList", Tag.TAG_STRING);
            Set<ResourceLocation> res = new HashSet<>();
            for (int i = 0; i < l.size(); i++) {
                res.add(new ResourceLocation(l.getString(i)));
            }
            return res;
        } else {
            return new HashSet<>();
        }
    }

    public static void setConfiguredTagList(ItemStack stack, Set<ResourceLocation> tags) {
        ListTag l = new ListTag();
        tags.forEach(rl -> l.add(StringTag.valueOf(rl.toString())));
        stack.getOrCreateTag().put("TagList", l);
    }

    @Override
    public boolean matchTags(ItemStack filterStack, Item item) {
        Set<ResourceLocation> tags = getConfiguredTagList(filterStack);
        return !Sets.intersection(tags, item.getTags()).isEmpty();
    }
}
