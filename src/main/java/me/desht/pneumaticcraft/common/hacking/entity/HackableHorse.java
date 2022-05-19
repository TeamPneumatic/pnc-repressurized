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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Horses, although tameable animals, don't extend TamableAnimal.  Yay.
 */
public class HackableHorse extends HackableTameable {

    private static final ResourceLocation ID = RL("horse");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return !player.getUUID().equals(((Horse) entity).getOwnerUUID());
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (entity.level.isClientSide) {
            entity.handleEntityEvent((byte) 7);
        } else {
            Horse horse = (Horse) entity;
            horse.getNavigation().stop();
            horse.setTarget(null);
            horse.setHealth(20.0F);
            horse.setOwnerUUID(player.getUUID());
            horse.level.broadcastEntityEvent(entity, (byte) 7);
            horse.setTamed(true);
        }
    }

}
