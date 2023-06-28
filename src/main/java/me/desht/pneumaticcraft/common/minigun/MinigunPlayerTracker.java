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

package me.desht.pneumaticcraft.common.minigun;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks various properties of the minigun item, without the overhead (especially for sync'ing) of using NBT
 * on the minigun item itself.
 */
public class MinigunPlayerTracker {
    private static final MinigunPlayerTracker clientInstance = new MinigunPlayerTracker();
    private static final Map<UUID,MinigunPlayerTracker> serverInstances = new HashMap<>();

    private boolean isActivated;
    private float barrelRotation;
    private float prevBarrelRotation;
    private int triggerTimeout;
    private float rotationSpeed;
    private int ammoColor;

    public static MinigunPlayerTracker getInstance(Player player) {
        return player.level().isClientSide ? clientInstance : serverInstances.computeIfAbsent(player.getUUID(), k -> new MinigunPlayerTracker());
    }

    private MinigunPlayerTracker() {
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public float getBarrelRotation() {
        return barrelRotation;
    }

    public void setBarrelRotation(float barrelRotation) {
        this.barrelRotation = barrelRotation;
    }

    public float getPrevBarrelRotation() {
        return prevBarrelRotation;
    }

    public void setPrevBarrelRotation(float prevBarrelRotation) {
        this.prevBarrelRotation = prevBarrelRotation;
    }

    public int getTriggerTimeout() {
        return triggerTimeout;
    }

    public void setTriggerTimeout(int triggerTimeout) {
        this.triggerTimeout = triggerTimeout;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public int getAmmoColor() {
        return ammoColor;
    }

    public void setAmmoColor(int ammoColor) {
        this.ammoColor = ammoColor;
    }
}
