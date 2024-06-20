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

package me.desht.pneumaticcraft.common.thirdparty.thaumcraft;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class BlockTrackEntryThaumcraft implements IBlockTrackEntry {
    public static final ResourceLocation ID = RL("block_tracker_module_thaumcraft");

    @Override
    public boolean shouldTrackWithThisEntry(Level world, BlockPos pos, BlockState state, BlockEntity te) {
        return false;
        // return te instance IAspectContainer
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        return Collections.emptyList();
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
//        if (te instanceof IAspectContainer) {
//            IAspectContainer container = (IAspectContainer)te;
//            AspectList aspects = container.getAspects();
//            if (aspects != null && aspects.size() > 0) {
//                infoList.add("pneumaticcraft.blockTracker.info.thaumcraft");
//                for(Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
//                    infoList.add("\u2022 " + entry.getValue() + " x " + entry.getKey().getName());
//                }
//            } else {
//                infoList.add(I18n.format("pneumaticcraft.blockTracker.info.thaumcraft.none"));
//            }
//        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
