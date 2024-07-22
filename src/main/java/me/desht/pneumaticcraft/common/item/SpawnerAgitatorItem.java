package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SpawnerAgitatorItem extends SemiblockItem {
    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);

        if (ModList.get().isLoaded(ModIds.APOTHEOSIS)) {
            pTooltipComponents.add(xlate("gui.tooltip.item.pneumaticcraft.spawner_agitator.apotheosis").withStyle(ChatFormatting.GOLD));
        }
    }
}
