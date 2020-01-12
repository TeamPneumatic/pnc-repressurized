package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;

public class MovingSoundJetBoots extends TickableSound {
    private final PlayerEntity player;
    private final CommonArmorHandler handler;
    private float targetPitch;
    private int endTimer = -1;

    MovingSoundJetBoots(PlayerEntity player) {
        super(ModSounds.LEAKING_GAS_LOW.get(), SoundCategory.NEUTRAL);

        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.5F;
        this.targetPitch = 0.7F;
        this.pitch = 0.4F;

        handler = CommonArmorHandler.getHandlerForPlayer(player);
    }

    @Override
    public void tick() {
        if (!handler.isValid() || !handler.isArmorEnabled()) {
            // handler gets invalidated if the tracked player disconnects
            donePlaying = true;
            return;
        }

        if (!handler.isJetBootsEnabled() && endTimer == -1 || !handler.isJetBootsActive() && player.onGround && endTimer == -1) {
            endTimer = 20;
        }
        if (endTimer > 0 && --endTimer <= 0) {
            donePlaying = true;
        }

        x = (float) player.posX;
        y = (float) player.posY;
        z = (float) player.posZ;

        if (endTimer > 0) {
            targetPitch = 0.5F;
            volume = 0.5F - ((20 - endTimer) / 50F);
        } else {
            if (handler.isJetBootsActive()) {
                double vel = player.getMotion().length();
                targetPitch = 0.7F + (float) vel / 15;
                volume = 0.5F + (float) vel / 15;
            } else {
                targetPitch = 0.5F;
                volume = 0.4F;
            }
        }
        pitch += (targetPitch - pitch) / 10F;
        if (player.isInWater()) {
            pitch *= 0.75f;
            volume *= 0.5f;
        }
    }
}
