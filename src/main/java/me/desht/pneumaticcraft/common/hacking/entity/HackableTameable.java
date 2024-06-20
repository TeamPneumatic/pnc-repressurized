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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableTameable extends AbstractTameableHack<TamableAnimal> {
    private static final ResourceLocation ID = RL("tameable");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<TamableAnimal> getHackableClass() {
        return TamableAnimal.class;
    }

    @Override
    public void onHackFinished(TamableAnimal entity, Player player) {
        if (entity.level().isClientSide) {
            entity.handleEntityEvent((byte) 7);
        } else {
            entity.getNavigation().stop();
            entity.setTarget(null);
            entity.setHealth(20.0F);
            entity.setOwnerUUID(player.getUUID());
            entity.level().broadcastEntityEvent(entity, EntityEvent.TAMING_SUCCEEDED);
            entity.setTame(true, true);

            // TODO: code smell
            // Would be better to have a HackableCat subclass, but HackableHandler.getHackableForEntity() isn't
            // set up to prioritise getting a cat over a generic tameable.
            if (entity instanceof Cat cat) {
                BuiltInRegistries.CAT_VARIANT.getTag(CatVariantTags.DEFAULT_SPAWNS)
                        .flatMap(variants -> variants.getRandomElement(cat.level().getRandom()))
                        .ifPresent(cat::setVariant);
            }
        }
    }
}
