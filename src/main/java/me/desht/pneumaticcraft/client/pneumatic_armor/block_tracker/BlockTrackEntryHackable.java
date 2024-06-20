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

package me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryHackable implements IBlockTrackEntry {
    public static final ResourceLocation ID = RL("block_tracker.module.hackables");

    @Override
    public boolean shouldTrackWithThisEntry(Level world, BlockPos pos, BlockState state, BlockEntity te) {
        return HackClientHandler.enabledForPlayer(ClientUtils.getClientPlayer())
                && HackManager.getHackableForBlock(world, pos, ClientUtils.getClientPlayer()) != null;
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        return Collections.emptyList();
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
        Player player = ClientUtils.getClientPlayer();
        IHackableBlock hackableBlock = HackManager.getHackableForBlock(world, pos, player);
        assert hackableBlock != null;
        int hackTime = ClientArmorRegistry.getInstance()
                .getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class)
                .getTargetForCoord(pos).getHackTime();
        if (hackTime == 0) {
            hackableBlock.addInfo(world, pos, infoList, player);
            HackClientHandler.addKeybindTooltip(infoList);
        } else {
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            int percentageComplete = hackTime * 100 / requiredHackTime;
            if (percentageComplete < 100) {
                infoList.add(xlate("pneumaticcraft.armor.hacking.hacking", percentageComplete));
            } else if (hackTime < requiredHackTime + 20) {
                hackableBlock.addPostHackInfo(world, pos, infoList, player);
            } else {
                hackableBlock.addInfo(world, pos, infoList, player);
                HackClientHandler.addKeybindTooltip(infoList);
            }
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
