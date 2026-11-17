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

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import org.jetbrains.annotations.NotNull;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableTameable extends AbstractTameableHack<TamableAnimal> {
    private static final Identifier ID = RL("tameable");

    @Override
    public Identifier getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<TamableAnimal> getHackableClass() {
        return TamableAnimal.class;
    }

    @Override
    public void onHackFinished(TamableAnimal entity, Player player) {
        if (entity.level().isClientSide()) {
            entity.handleEntityEvent((byte) 7);
        } else {
            entity.getNavigation().stop();
            entity.setTarget(null);
            entity.setHealth(20.0F);
            entity.setOwner(player);
            entity.level().broadcastEntityEvent(entity, EntityEvent.TAMING_SUCCEEDED);
            entity.setTame(true, true);

            // TODO: code smell
            // Would be better to have a HackableCat subclass, but HackableHandler.getHackableForEntity() isn't
            // set up to prioritise getting a cat over a generic tameable.
            // cat variants are data-driven now: pick one as for a natural spawn at the cat's position
            if (entity instanceof Cat cat && cat.level() instanceof ServerLevel serverLevel) {
                VariantUtils.selectVariantToSpawn(SpawnContext.create(serverLevel, cat.blockPosition()), Registries.CAT_VARIANT)
                        .ifPresent(variant -> cat.setComponent(DataComponents.CAT_VARIANT, variant));
            }
        }
    }
}
