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

package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableDoor implements IHackableBlock {
    private static final ResourceLocation ID = RL("door");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(BlockGetter level, BlockPos pos, BlockState state, Player player) {
        return level.getBlockState(pos).hasProperty(getOpenProperty());
    }

    @Override
    public void addInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player) {
        if (world.getBlockState(pos).getValue(getOpenProperty())) {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.result.close"));
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.result.open"));
        }
    }

    @Override
    public void addPostHackInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player) {
        if (world.getBlockState(pos).getValue(getOpenProperty())) {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.opened"));
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.closed"));
        }
    }

    @Override
    public int getHackTime(BlockGetter world, BlockPos pos, Player player) {
        return 20;
    }

    @Override
    public void onHackComplete(Level world, BlockPos pos, Player player) {
        BlockState state = world.getBlockState(pos);
        fakeRayTrace(player, pos).ifPresent(rtr -> state.useWithoutItem(world, player, rtr));
    }

    protected BooleanProperty getOpenProperty() {
        return DoorBlock.OPEN;
    }
}
