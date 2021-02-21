package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;

public class MovingSoundJetBoots extends TickableSound {
    private static final int END_TICKS = 20;

    private final PlayerEntity player;
    private final CommonArmorHandler handler;
    private float targetPitch;
    private int endTimer = Integer.MAX_VALUE;

    MovingSoundJetBoots(PlayerEntity player) {
        super(ModSounds.LEAKING_GAS_LOW.get(), SoundCategory.NEUTRAL);

        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.targetPitch = 0.7F;
        this.pitch = 0.5F;
        this.handler = CommonArmorHandler.getHandlerForPlayer(player);
        this.volume = volumeFromConfig();
    }

    @Override
    public void tick() {
        if (!handler.isValid() || !handler.isArmorEnabled()) {
            // handler gets invalidated if the tracked player disconnects
            return;
        }

        if (endTimer == Integer.MAX_VALUE &&
                (!handler.isJetBootsEnabled()
                        || (!handler.isJetBootsActive() && (player.isOnGround() || player.isElytraFlying())))) {
            endTimer = END_TICKS;
        }
        if (endTimer <= END_TICKS) {
            if (player.isOnGround() || !handler.isJetBootsActive()) {
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
            volume = volumeFromConfig() - ((END_TICKS - endTimer) / 50F);
        } else {
            if (handler.isJetBootsActive()) {
                double vel = player.getMotion().length();
                targetPitch = 0.9F + (float) vel / 15;
                volume = volumeFromConfig() + (float) vel / 15;
            } else {
                targetPitch = 0.9F;
                volume = volumeFromConfig() * 0.8F;
            }
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

    private float volumeFromConfig() {
        return (float) (handler.isJetBootsBuilderMode() ? PNCConfig.Client.Sound.jetbootsVolumeBuilderMode : PNCConfig.Client.Sound.jetbootsVolume);
    }
}
