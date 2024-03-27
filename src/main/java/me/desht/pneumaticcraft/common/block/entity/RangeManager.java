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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RangeManager {
    private final BlockEntity te;
    private final int renderColour;
    private int range = 0;
    private boolean showRange = false;
    private BoundingBox extents;
    private Supplier<BoundingBox> extentsGenerator;
    private Set<BlockPos> frame;  // for rendering

    public RangeManager(BlockEntity te, int renderColour) {
        this.te = te;
        this.renderColour = renderColour;
        this.extentsGenerator = () -> new BoundingBox(te.getBlockPos()).inflatedBy(range);
        this.setRange(1);
    }

    public RangeManager withCustomExtents(Supplier<BoundingBox> generator) {
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
            this.frame = te.getLevel() != null && te.getLevel().isClientSide() ? getFrame(extents) : Set.of();
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

    public BoundingBox getExtents() {
        return extents;
    }

    public AABB getExtentsAsAABB() {
        return new AABB(extents.minX(), extents.minY(), extents.minZ(), extents.maxX(), extents.maxY(), extents.maxZ());
    }

    public static Set<BlockPos> getFrame(BlockPos pos, int range) {
        return getFrame(new BoundingBox(pos).inflatedBy(range));
    }

    public static Set<BlockPos> getFrame(BoundingBox extents) {
        Set<BlockPos> res = new HashSet<>();
        int minX = extents.minX();
        int minY = extents.minY();
        int minZ = extents.minZ();
        int maxX = extents.maxX();
        int maxY = extents.maxY();
        int maxZ = extents.maxZ();
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
