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

import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for all PneumaticCraft block items.
 * <p>
 * Vanilla no longer has {@code Block#appendHoverText()}; PneumaticCraft blocks keep their own
 * {@link AbstractPneumaticCraftBlock#appendHoverText(ItemStack, TooltipContext, List, TooltipFlag)},
 * and this item passes the block's tooltip lines through to the item tooltip.
 */
public class PNCBlockItem extends BlockItem {
    public PNCBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        if (getBlock() instanceof AbstractPneumaticCraftBlock pncBlock) {
            List<Component> blockInfo = new ArrayList<>();
            pncBlock.appendHoverText(stack, context, blockInfo, flag);
            blockInfo.forEach(tooltip);
        }
    }
}
