package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

public class MovingSounds {
    public enum Sound {
        JET_BOOTS,
        MINIGUN,
        ELEVATOR,
        AIR_LEAK,
        JACKHAMMER
    }

    // track existing moving sound objects for blocks that can't easily tell when to start playing a sound
    private static final Map<BlockPos, TickableSound> posToTickableSound = new HashMap<>();

    private static TickableSound createMovingSound(Sound s, Object focus, Object... extraData) {
        switch (s) {
            case JET_BOOTS:
                if (focus instanceof PlayerEntity) {
                    return new MovingSoundJetBoots((PlayerEntity) focus);
                }
                break;
            case MINIGUN:
                if (focus instanceof PlayerEntity || focus instanceof EntityDrone) {
                    return new MovingSoundMinigun((Entity) focus);
                } else if (focus instanceof BlockPos) {
                    TileEntity te = Minecraft.getInstance().world.getTileEntity((BlockPos) focus);
                    return te == null ? null : new MovingSoundMinigun(te);
                }
                break;
            case ELEVATOR:
                if (focus instanceof BlockPos) {
                    TileEntity te = Minecraft.getInstance().world.getTileEntity((BlockPos) focus);
                    if (te instanceof TileEntityElevatorBase) {
                        return new MovingSoundElevator((TileEntityElevatorBase) te);
                    }
                }
                break;
            case AIR_LEAK:
                if (focus instanceof BlockPos) {
                    TickableSound sound = posToTickableSound.get(focus);
                    if (sound != null && !sound.isDonePlaying()) {
                        return null;  // a sound is still playing; don't start another one
                    }
                    TileEntity te = Minecraft.getInstance().world.getTileEntity((BlockPos) focus);
                    if (te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).isPresent()) {
                        sound = new MovingSoundAirLeak(te, (Direction) extraData[0]);
                        posToTickableSound.put((BlockPos) focus, sound);
                        return sound;
                    }
                }
                break;
            case JACKHAMMER:
                if (focus instanceof PlayerEntity) {
                    return MovingSoundJackhammer.startOrContinue((PlayerEntity) focus);
                }
        }
        throw new IllegalArgumentException("Invalid moving sound " + s + " for focus object " + focus);
    }

    public static void playMovingSound(Sound s, Object focus, Object... extraData) {
        TickableSound movingSound = createMovingSound(s, focus, extraData);
        if (movingSound != null) {
            Minecraft.getInstance().getSoundHandler().play(movingSound);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    private static class Listener {
        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
            if (event.getWorld().isRemote && event.getEntity() instanceof ClientPlayerEntity) {
                posToTickableSound.clear();
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            posToTickableSound.values().removeIf(TickableSound::isDonePlaying);
        }
    }
}
