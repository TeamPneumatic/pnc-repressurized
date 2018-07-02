package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class MovingSoundJetBoots extends MovingSound {
    private final EntityPlayer player;
    private final CommonHUDHandler handler;
    private float targetPitch;
    private int endTimer = -1;

    public MovingSoundJetBoots(EntityPlayer player) {
        super(Sounds.LEAKING_GAS_SOUND, SoundCategory.NEUTRAL);

        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.2F;
        this.targetPitch = 0.7F;
        this.pitch = 0.4F;

        handler = CommonHUDHandler.getHandlerForPlayer(player);
    }

    @Override
    public void update() {
        if (!handler.isValid() || !handler.isArmorEnabled()) {
            // handler gets invalidated if the tracked player disconnects
            donePlaying = true;
            return;
        }

        if (!handler.isJetBootsActive() && player.onGround && endTimer == -1) {
            endTimer = 20;
        }
        if (endTimer > 0 && --endTimer <= 0) {
            donePlaying = true;
        }

        xPosF = (float) player.posX;
        yPosF = (float) player.posY;
        zPosF = (float) player.posZ;

        if (endTimer > 0) {
            targetPitch = 0.5F;
            volume = 0.2F - ((20 - endTimer) / 100F);
        } else {
            if (handler.isJetBootsActive()) {
                double vel = Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ);
                targetPitch = 0.7F + (float) vel / 20;
            } else {
                targetPitch = 0.6F;
            }
        }
        pitch += (targetPitch - pitch) / 10F;
    }
}
