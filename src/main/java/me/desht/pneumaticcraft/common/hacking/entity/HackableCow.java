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

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableCow implements IHackableEntity<Cow> {
    private static final ResourceLocation ID = RL("cow");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Cow> getHackableClass() {
        return Cow.class;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return IHackableEntity.super.canHack(entity, player) && !(entity instanceof MushroomCow);
    }

    @Override
    public void addHackInfo(Cow entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.fungiInfuse"));
    }

    @Override
    public void addPostHackInfo(Cow entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.fungiInfusion"));
    }

    @Override
    public int getHackTime(Cow entity, Player player) {
        return 100;
    }

    @Override
    public void onHackFinished(Cow entity, Player player) {
        if (!entity.level().isClientSide) {
            entity.discard();
            MushroomCow entitycow = new MushroomCow(EntityType.MOOSHROOM, entity.level());
            entitycow.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            entitycow.setHealth(entity.getHealth());
            entitycow.yBodyRot = entity.yBodyRot;
            entity.level().addFreshEntity(entitycow);
            entity.level().addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0F, entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

}
