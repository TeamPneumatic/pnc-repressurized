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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.entity.ElevatorBaseBlockEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
    private static final Map<BlockPos, AbstractTickableSoundInstance> posToTickableSound = new HashMap<>();

    private static AbstractTickableSoundInstance createMovingSound(Sound s, Object focus, Object... extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return null;

        switch (s) {
            case JET_BOOTS -> {
                if (focus instanceof Player player) {
                    return new MovingSoundJetBoots(player);
                }
            }
            case MINIGUN -> {
                if (focus instanceof Player || focus instanceof DroneEntity) {
                    return new MovingSoundMinigun((Entity) focus);
                } else if (focus instanceof BlockPos pos) {
                    BlockEntity te = world.getBlockEntity(pos);
                    return te == null ? null : new MovingSoundMinigun(te);
                }
            }
            case ELEVATOR -> {
                if (focus instanceof BlockPos pos) {
                    BlockEntity te = world.getBlockEntity(pos);
                    return te instanceof ElevatorBaseBlockEntity ? new MovingSoundElevator((ElevatorBaseBlockEntity) te) : null;
                }
            }
            case AIR_LEAK -> {
                if (focus instanceof BlockPos pos) {
                    AbstractTickableSoundInstance sound = posToTickableSound.get(pos);
                    if (sound != null && !sound.isStopped()) {
                        return null;  // a sound is still playing; don't start another one
                    }
                    BlockEntity te = world.getBlockEntity(pos);
                    if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).isPresent()) {
                        sound = new MovingSoundAirLeak(te, (Direction) extraData[0]);
                        posToTickableSound.put(pos, sound);
                        return sound;
                    }
                } else {
                    return null;
                }
            }
            case JACKHAMMER -> {
                if (focus instanceof Player player) {
                    return MovingSoundJackhammer.startOrContinue(player);
                }
            }
        }
        throw new IllegalArgumentException("Invalid moving sound " + s + " for focus object " + focus);
    }

    public static void playMovingSound(Sound s, Object focus, Object... extraData) {
        AbstractTickableSoundInstance movingSound = createMovingSound(s, focus, extraData);
        if (movingSound != null) {
            Minecraft.getInstance().getSoundManager().play(movingSound);
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    private static class Listener {
        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide && event.getEntity() instanceof LocalPlayer) {
                posToTickableSound.clear();
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            posToTickableSound.values().removeIf(AbstractTickableSoundInstance::isStopped);
        }
    }
}
