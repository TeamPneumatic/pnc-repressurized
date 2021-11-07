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

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public class OilLakePlacement extends Placement<ChanceConfig> {
    public OilLakePlacement(Codec<ChanceConfig> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(WorldDecoratingHelper helper, Random rand, ChanceConfig chanceConfig, BlockPos pos) {
        if (rand.nextInt(100) < chanceConfig.chance) {
            int x = rand.nextInt(16) + pos.getX();
            int z = rand.nextInt(16) + pos.getZ();
            int y = rand.nextInt(rand.nextInt(helper.getGenDepth() - 8) + 8);
            // if position is not below sea level, reduced random chance for surface lake
            if (y < helper.getSeaLevel() || rand.nextInt(100) < PNCConfig.Common.General.surfaceOilGenerationChance) {
                return Stream.of(new BlockPos(x, y, z));
            }
        }

        return Stream.empty();
    }
}
