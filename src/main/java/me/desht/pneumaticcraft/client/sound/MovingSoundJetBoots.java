package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class MovingSoundJetBoots extends MovingSound {
    private final EntityPlayer player;
    private final CommonHUDHandler handler;
    private int endTimer = -1;

    public MovingSoundJetBoots(EntityPlayer player) {
        super(Sounds.LEAKING_GAS_SOUND, SoundCategory.NEUTRAL);

        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.2F;

        handler = CommonHUDHandler.getHandlerForPlayer(player);
    }

    @Override
    public void update() {
        if (!handler.isValid()) {
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
            pitch = 0.6F - ((20 - endTimer) / 50F);
            volume = 0.2F - ((20 - endTimer) / 100F);
        } else {
            pitch = handler.isJetBootsActive() ? 0.7F : 0.6F;
        }
    }
}
