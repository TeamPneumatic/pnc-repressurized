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

package me.desht.pneumaticcraft.common.hacking.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Horses, although tameable, don't extend EntityTameable.  Yay.
 */
public class HackableHorse extends HackableTameable {
    @Override
    public ResourceLocation getHackableId() {
        return RL("horse");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return !player.getUUID().equals(((HorseEntity) entity).getOwnerUUID());
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (entity.level.isClientSide) {
            entity.handleEntityEvent((byte) 7);
        } else {
            HorseEntity horse = (HorseEntity) entity;
            horse.getNavigation().stop();
            horse.setTarget(null);
            horse.setHealth(20.0F);
            horse.setOwnerUUID(player.getUUID());
            horse.level.broadcastEntityEvent(entity, (byte) 7);
            horse.setTamed(true);
        }
    }

}
