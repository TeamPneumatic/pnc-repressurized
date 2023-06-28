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

package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MovingSoundJetBoots extends AbstractTickableSoundInstance {
    private static final int END_TICKS = 20;
    private static final Vec3 IDLE_VEC = new Vec3(0, -0.5, 0);

    private final Player player;
    private float targetPitch;
    private int endTimer = Integer.MAX_VALUE;

    public MovingSoundJetBoots(Player player) {
        super(ModSounds.LEAKING_GAS_LOW.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());

        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.targetPitch = 0.7F;
        this.pitch = 0.5F;
        this.volume = volumeFromConfig(JetBootsStateTracker.getClientTracker().getJetBootsState(player).isBuilderMode());

        // kludge: tiny bit of upward displacement allows player to walk off an edge and back on again
        // (when smart hover isn't being used)
        // 0.1 displacement is too small to be perceptible to player
        player.setPos(player.getX(), player.getY() + 0.1, player.getZ());
    }

    @Override
    public void tick() {
        if (!player.isAlive()) {
            return;
        }

        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);

        if (endTimer == Integer.MAX_VALUE &&
                (!jbState.isEnabled() || (!jbState.isActive() && (player.onGround() || player.isFallFlying())))) {
            endTimer = END_TICKS;
        }
        if (endTimer <= END_TICKS) {
            if (player.onGround() || !jbState.isActive()) {
                endTimer--;
            } else {
                endTimer = Integer.MAX_VALUE;
            }
        }

        x = (float) player.getX();
        y = (float) player.getY();
        z = (float) player.getZ();

        if (endTimer > 0 && endTimer <= END_TICKS) {
            targetPitch = 0.5F;
            volume = volumeFromConfig(jbState.isBuilderMode()) - ((END_TICKS - endTimer) / 50F);
        } else {
            if (jbState.isActive()) {
                double vel = player.getDeltaMovement().length();
                targetPitch = 0.9F + (float) vel / 15;
                volume = volumeFromConfig(jbState.isBuilderMode()) + (float) vel / 15;
            } else {
                targetPitch = 0.9F;
                volume = volumeFromConfig(jbState.isBuilderMode()) * 0.8F;
            }
            handleParticles(jbState.isActive(), jbState.isBuilderMode());
        }
        pitch += (targetPitch - pitch) / 10F;
        if (player.isInWater()) {
            pitch *= 0.75f;
            volume *= 0.5f;
        }
    }

    @Override
    public boolean isStopped() {
        return !player.isAlive() || endTimer <= 0;
    }

    private void handleParticles(boolean jetBootsActive, boolean builderMode) {
        int distThresholdSq = ClientUtils.getRenderDistanceThresholdSq();
        if ((jetBootsActive || (player.level().getGameTime() & 0x3) == 0 || !ClientUtils.isFirstPersonCamera()) && player.distanceToSqr(ClientUtils.getClientPlayer()) < distThresholdSq) {
            int nParticles = jetBootsActive ? 3 : 1;
            Vec3 jetVec = jetBootsActive && !builderMode ? player.getLookAngle().scale(-0.5) : IDLE_VEC;
            Vec3 feet = jetBootsActive && !builderMode ?
                    player.position().add(player.getLookAngle().scale(player == ClientUtils.getClientPlayer() ? -4 : -2)) :
                    player.position().add(0, -0.25, 0);
            for (int i = 0; i < nParticles; i++) {
                player.level().addParticle(AirParticleData.DENSE, feet.x, feet.y, feet.z, jetVec.x, jetVec.y, jetVec.z);
            }
        }
    }

    private float volumeFromConfig(boolean builderMode) {
        return builderMode ? ConfigHelper.client().sound.jetbootsVolumeBuilderMode.get().floatValue() : ConfigHelper.client().sound.jetbootsVolume.get().floatValue();
    }
}
