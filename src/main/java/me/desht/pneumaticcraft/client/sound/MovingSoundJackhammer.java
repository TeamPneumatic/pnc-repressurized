package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovingSoundJackhammer extends TickableSound {
    private final PlayerEntity player;
    private static final Map<UUID, Long> timers = new HashMap<>();

    public MovingSoundJackhammer(PlayerEntity player) {
        super(ModSounds.JACKHAMMER_LOOP.get(), SoundCategory.PLAYERS);
        this.player = player;
        x = player.getPosX();
        y = player.getPosY();
        z = player.getPosZ();
        repeat = true;
        repeatDelay = 0;
        volume = (float) PNCConfig.Client.Sound.jackhammerVolume;
    }

    static MovingSoundJackhammer startOrContinue(PlayerEntity player) {
        MovingSoundJackhammer res;
        if (lastJackHammerTime(player) > 15) {
            res = new MovingSoundJackhammer(player);
        } else {
            res = null;  // don't start another one
        }
        timers.put(player.getUniqueID(), player.world.getGameTime());
        return res;
    }

    @Override
    public void tick() {
        x = player.getPosX();
        y = player.getPosY();
        z = player.getPosZ();
        if (lastJackHammerTime(player) == 15) {
            player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(),
                    ModSounds.JACKHAMMER_STOP.get(), SoundCategory.PLAYERS,
                    (float) PNCConfig.Client.Sound.jackhammerVolume, 1f);
        }
    }

    @Override
    public boolean isDonePlaying() {
        return lastJackHammerTime(player) > 15;
    }

    public static long lastJackHammerTime(PlayerEntity player) {
        return player.getEntityWorld().getGameTime() - timers.getOrDefault(player.getUniqueID(), 0L);
    }
}
