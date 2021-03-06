package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class MovingSoundJetBoots extends TickableSound {
    private static final int END_TICKS = 20;
    private static final Vector3d IDLE_VEC = new Vector3d(0, -0.5, 0);

    private final PlayerEntity player;
    private final CommonArmorHandler handler;
    private float targetPitch;
    private int endTimer = Integer.MAX_VALUE;

    public MovingSoundJetBoots(PlayerEntity player) {
        super(ModSounds.LEAKING_GAS_LOW.get(), SoundCategory.NEUTRAL);

        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.targetPitch = 0.7F;
        this.pitch = 0.5F;
        this.handler = CommonArmorHandler.getHandlerForPlayer(player);
        this.volume = volumeFromConfig(JetBootsStateTracker.getClientTracker().getJetBootsState(player).isBuilderMode());
    }

    @Override
    public void tick() {
        if (!handler.isValid() || !handler.isArmorEnabled()) {
            // handler gets invalidated if the tracked player disconnects
            return;
        }

        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);

        if (endTimer == Integer.MAX_VALUE &&
                (!jbState.isEnabled() || (!jbState.isActive() && (player.isOnGround() || player.isElytraFlying())))) {
            endTimer = END_TICKS;
        }
        if (endTimer <= END_TICKS) {
            if (player.isOnGround() || !jbState.isActive()) {
                endTimer--;
            } else {
                endTimer = Integer.MAX_VALUE;
            }
        }

        x = (float) player.getPosX();
        y = (float) player.getPosY();
        z = (float) player.getPosZ();

        if (endTimer > 0 && endTimer <= END_TICKS) {
            targetPitch = 0.5F;
            volume = volumeFromConfig(jbState.isBuilderMode()) - ((END_TICKS - endTimer) / 50F);
        } else {
            if (jbState.isActive()) {
                double vel = player.getMotion().length();
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
    public boolean isDonePlaying() {
        return !handler.isValid() || !handler.isArmorEnabled() || endTimer <= 0;
    }

    private void handleParticles(boolean jetBootsActive, boolean builderMode) {
        int distThresholdSq = ClientUtils.getRenderDistanceThresholdSq();
        if ((jetBootsActive || (player.world.getGameTime() & 0x3) == 0 || !ClientUtils.isFirstPersonCamera()) && player.getDistanceSq(ClientUtils.getClientPlayer()) < distThresholdSq) {
            int nParticles = jetBootsActive ? 3 : 1;
            Vector3d jetVec = jetBootsActive && !builderMode ? player.getLookVec().scale(-0.5) : IDLE_VEC;
            Vector3d feet = jetBootsActive && !builderMode ?
                    player.getPositionVec().add(player.getLookVec().scale(player == ClientUtils.getClientPlayer() ? -4 : -2)) :
                    player.getPositionVec().add(0, -0.25, 0);
            for (int i = 0; i < nParticles; i++) {
                player.world.addParticle(AirParticleData.DENSE, feet.x, feet.y, feet.z, jetVec.x, jetVec.y, jetVec.z);
            }
        }
    }

    private float volumeFromConfig(boolean builderMode) {
        return (float) (builderMode ? PNCConfig.Client.Sound.jetbootsVolumeBuilderMode : PNCConfig.Client.Sound.jetbootsVolume);
    }
}
