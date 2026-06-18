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
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.world.level.block.SculkSensorBlock.PHASE;
import static net.minecraft.world.level.block.state.properties.SculkSensorPhase.COOLDOWN;

public class HackableSculkSensor implements IHackableBlock {
    private static final ResourceLocation ID = RL("sculk_sensor");
    private static final int SILENCE_TIME = 200;

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public void addInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.silence"));
    }

    @Override
    public void addPostHackInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.silenced"));
    }

    @Override
    public int getHackTime(BlockGetter world, BlockPos pos, Player player) {
        return 80;
    }

    @Override
    public void onHackComplete(Level world, BlockPos pos, Player player) {
        if (world instanceof ServerLevel serverLevel) {
            BlockState state = world.getBlockState(pos);

            // set it to inactive initially, so any pending ticks don't just wake it up again
            world.setBlock(pos, state.setValue(PHASE, SculkSensorPhase.INACTIVE), Block.UPDATE_ALL);

            // and once the cooldown duration is passed, set it into cooldown mode for the silencing duration
            serverLevel.getServer().tell(new TickTask(serverLevel.getServer().getTickCount() + SculkSensorBlock.COOLDOWN_TICKS + 1, () -> {
                if (world.getBlockState(pos).getBlock() instanceof SculkSensorBlock) {
                    world.setBlock(pos, state.setValue(PHASE, COOLDOWN).setValue(SculkSensorBlock.POWER, 0), Block.UPDATE_ALL);
                    world.scheduleTick(pos, state.getBlock(), SILENCE_TIME);
                    world.updateNeighborsAt(pos, state.getBlock());
                    world.updateNeighborsAt(pos.below(), state.getBlock());
                }
            }));
        }
    }

    @Override
    public boolean canHack(BlockGetter level, BlockPos pos, BlockState state, Player player) {
        return state.hasProperty(PHASE) && state.getValue(PHASE) != COOLDOWN;
    }
}
