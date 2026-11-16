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
package me.desht.pneumaticcraft.client.model;

import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Wraps the block state model of camouflageable blocks; when the block entity reports a camouflage state through
 * its model data, the camouflage's own model parts are collected instead. Since 26.1 the chunk layer is a property
 * of each quad, so there is no per-render-type filtering any more.
 * <p>
 * With credit to Vazkii for the original idea:
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/model/PlatformModel.java
 */
public class CamoModel extends DelegateBlockStateModel {
    public CamoModel(BlockStateModel originalModel) {
        super(originalModel);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        BlockState camoState = getCamoState(level, pos, state);
        if (camoState == null) {
            super.collectParts(level, pos, state, random, parts);
        } else {
            getModel(camoState).collectParts(level, pos, camoState, random, parts);
        }
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState camoState = getCamoState(level, pos, state);
        return camoState == null ? super.materialFlags(level, pos, state) : getModel(camoState).materialFlags(level, pos, camoState);
    }

    @Nullable
    private static BlockState getCamoState(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof AbstractCamouflageBlock)) {
            return null;
        }
        return level.getModelData(pos).get(AbstractCamouflageBlock.CAMO_STATE);
    }

    private static BlockStateModel getModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }
}
