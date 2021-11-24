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

package me.desht.pneumaticcraft.common.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.LakesFeature;

import java.util.Random;

/**
 * Extended vanilla lake feature to allow blacklisting by dimension ID
 */
public class OilLakeFeature extends LakesFeature {
    public OilLakeFeature() {
        super(BlockStateFeatureConfig.CODEC);
    }

    @Override
    public boolean place(ISeedReader level, ChunkGenerator generator, Random rand, BlockPos pos, BlockStateFeatureConfig config) {
        return !ModWorldGen.isDimensionBlacklisted(level) && super.place(level, generator, rand, pos, config);
    }
}
