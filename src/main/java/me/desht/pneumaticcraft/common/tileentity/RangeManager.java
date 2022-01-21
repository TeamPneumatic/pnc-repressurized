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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RangeManager {
    private final BlockEntity te;
    private final int renderColour;
    private int range = 0;
    private boolean showRange = false;
    private AABB extents;
    private Supplier<AABB> extentsGenerator;
    private Set<BlockPos> frame;  // for rendering

    public RangeManager(BlockEntity te, int renderColour) {
        this.te = te;
        this.renderColour = renderColour;
        this.extentsGenerator = () -> new AABB(te.getBlockPos(), te.getBlockPos()).inflate(range);
        this.setRange(1);
    }

    public RangeManager withCustomExtents(Supplier<AABB> generator) {
        this.extentsGenerator = generator;
        return this;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int newRange) {
        if (newRange != range) {
            range = newRange;
            this.extents = extentsGenerator.get();
            this.frame = te.getLevel() != null && te.getLevel().isClientSide() ? getFrame(extents) : Collections.emptySet();
            if (shouldShowRange() && te.getLevel() != null && te.getLevel().isClientSide()) {
                toggleShowRange();
                toggleShowRange();
            }
        }
    }

    public void toggleShowRange() {
        showRange = !showRange;
        if (te.getLevel() != null && te.getLevel().isClientSide()) {
            if (showRange) {
                AreaRenderManager.getInstance().showArea(frame, renderColour, te, false);
            } else {
                AreaRenderManager.getInstance().removeHandlers(te);
            }
        }
    }

    public boolean shouldShowRange() {
        return showRange;
    }

    public AABB getExtents() {
        return extents;
    }

    public static Set<BlockPos> getFrame(AABB extents) {
        Set<BlockPos> res = new HashSet<>();
        int minX = (int) extents.minX;
        int minY = (int) extents.minY;
        int minZ = (int) extents.minZ;
        int maxX = (int) extents.maxX;
        int maxY = (int) extents.maxY;
        int maxZ = (int) extents.maxZ;
        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        res.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return res;
    }
}
