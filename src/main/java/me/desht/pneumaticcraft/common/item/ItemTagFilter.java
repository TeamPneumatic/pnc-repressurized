package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.api.item.ITagFilteringItem;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemTagFilter extends Item implements ITagFilteringItem {
    private static final String NBT_TAG_LIST = "TagList";

    public ItemTagFilter() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn != null) {
            tooltip.add(xlate("pneumaticcraft.gui.tooltip.tag_filter.header").withStyle(TextFormatting.YELLOW));
            for (ResourceLocation rl : getConfiguredTagList(stack)) {
                tooltip.add(Symbols.bullet().append(rl.toString()).withStyle(TextFormatting.GOLD));
            }
        }
    }

    public static Set<ResourceLocation> getConfiguredTagList(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains(NBT_TAG_LIST)) {
            ListNBT l = nbt.getList("TagList", Constants.NBT.TAG_STRING);
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
        ListNBT l = new ListNBT();
        tags.forEach(rl -> l.add(StringNBT.valueOf(rl.toString())));
        stack.getOrCreateTag().put("TagList", l);
    }

    @Override
    public boolean matchTags(ItemStack filterStack, Item item) {
        Set<ResourceLocation> tags = getConfiguredTagList(filterStack);
        return !Sets.intersection(tags, item.getTags()).isEmpty();
    }
}
