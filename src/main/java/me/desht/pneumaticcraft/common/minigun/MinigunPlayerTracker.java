package me.desht.pneumaticcraft.common.minigun;

import net.minecraft.entity.player.PlayerEntity;

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

    public static MinigunPlayerTracker getInstance(PlayerEntity player) {
        return player.world.isRemote ? clientInstance : serverInstances.computeIfAbsent(player.getUniqueID(), k -> new MinigunPlayerTracker());
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
