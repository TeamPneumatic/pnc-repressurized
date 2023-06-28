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
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Horses, although tameable animals, don't extend TamableAnimal, or even OwnableEntity.  Yay.
 */
public class HackableHorse extends AbstractTameableHack<Horse> {
    private static final ResourceLocation ID = RL("horse");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Horse> getHackableClass() {
        return Horse.class;
    }

    @Override
    public void onHackFinished(Horse entity, Player player) {
        if (entity.level().isClientSide) {
            entity.handleEntityEvent((byte) 7);
        } else {
            entity.getNavigation().stop();
            entity.setTarget(null);
            entity.setHealth(20.0F);
            entity.setOwnerUUID(player.getUUID());
            entity.level().broadcastEntityEvent(entity, (byte) 7);
            entity.setTamed(true);
        }
    }

}
