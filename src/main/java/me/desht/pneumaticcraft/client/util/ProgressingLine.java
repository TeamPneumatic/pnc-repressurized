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

package me.desht.pneumaticcraft.client.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ProgressingLine {
    public float startX;
    public float startY;
    public float startZ;
    public float endX;
    public float endY;
    public float endZ;
    protected float progress = 0;

    public ProgressingLine() {
    }

    public ProgressingLine(Vector3f start, Vector3f end) {
        this.startX = start.x();
        this.startY = start.y();
        this.startZ = start.z();
        this.endX = end.x();
        this.endY = end.y();
        this.endZ = end.z();
    }

    public ProgressingLine(Vec3 start, Vec3 end) {
        this(start.toVector3f(), end.toVector3f());
    }

    public ProgressingLine(float startX, float startY, float startZ, float endX, float endY, float endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public ProgressingLine(float startX, float startY, float endX, float endY) {
        this(new Vector3f(startX, startY, 0f), new Vector3f(endX, endY, 0f));
    }

    public float getProgress() {
        return progress;
    }

    public ProgressingLine setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    /**
     * Increases the progress by the given amount.
     *
     * @param increment
     * @return Returns true when the maximum progress has been reached.
     */
    public boolean incProgress(float increment) {
        progress += increment;
        if (progress > 1F) {
            progress = 1F;
            return true;
        } else if (progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }

    public boolean incProgressByDistance(double distance) {
        double totalDistance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2));
        progress += distance / totalDistance;
        if (progress > 1F) {
            progress = 1F;
            return true;
        } else if (progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }
}
