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

package me.desht.pneumaticcraft.common.item.logistics;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.item.PressurizableItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Stream;

public class LogisticsConfiguratorItem extends PressurizableItem {

    public LogisticsConfiguratorItem() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction side = ctx.getClickedFace();

        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player != null && PNCCapabilities.getAirHandler(stack).map(h -> h.getPressure() > 0.1).orElseThrow(RuntimeException::new)) {
            Stream<ISemiBlock> semiBlocks = SemiblockTracker.getInstance().getAllSemiblocks(world, pos, side);

            boolean didWork = false;
            if (player.isShiftKeyDown()) {
                List<ISemiBlock> l = semiBlocks.filter(s -> !(s instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) s).getSide() == side).toList();
                if (!l.isEmpty()) {
                    l.forEach(s -> s.killedByEntity(player));
                    didWork = true;
                }
            } else if (semiBlocks.anyMatch(s -> s.onRightClickWithConfigurator(player, side))) {
                didWork = true;
            }
            if (didWork) {
                if (!player.isCreative()) {
                    PNCCapabilities.getAirHandler(stack)
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
