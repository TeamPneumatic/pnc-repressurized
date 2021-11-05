package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
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
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        looping = true;
        delay = 0;
        volume = ConfigHelper.client().sound.jackhammerVolume.get().floatValue();
    }

    static MovingSoundJackhammer startOrContinue(PlayerEntity player) {
        MovingSoundJackhammer res;
        if (lastJackHammerTime(player) > 15) {
            res = new MovingSoundJackhammer(player);
        } else {
            res = null;  // don't start another one
        }
        timers.put(player.getUUID(), player.level.getGameTime());
        return res;
    }

    @Override
    public void tick() {
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        if (lastJackHammerTime(player) == 15) {
            player.level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    ModSounds.JACKHAMMER_STOP.get(), SoundCategory.PLAYERS,
                    ConfigHelper.client().sound.jackhammerVolume.get().floatValue(), 1f);
        }
    }

    @Override
    public boolean isStopped() {
        return lastJackHammerTime(player) > 15;
    }

    public static long lastJackHammerTime(PlayerEntity player) {
        return player.getCommandSenderWorld().getGameTime() - timers.getOrDefault(player.getUUID(), 0L);
    }
}
