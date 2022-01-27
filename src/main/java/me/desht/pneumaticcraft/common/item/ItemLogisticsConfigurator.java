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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class ItemLogisticsConfigurator extends ItemPressurizable {

    public ItemLogisticsConfigurator() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction side = ctx.getClickedFace();

        if (!world.isClientSide && player != null
                && stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> h.getPressure() > 0.1).orElseThrow(RuntimeException::new)) {
            Stream<ISemiBlock> semiBlocks = SemiblockTracker.getInstance().getAllSemiblocks(world, pos, side);

            if (player.isShiftKeyDown()) {
                semiBlocks.filter(s -> !(s instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) s).getSide() == side)
                        .forEach(s -> s.removeSemiblock(player));
                return InteractionResult.SUCCESS;
            } else {
                if (semiBlocks.anyMatch(s -> s.onRightClickWithConfigurator(player, side))) {
                    stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
