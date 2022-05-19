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

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableCow implements IHackableEntity {

    private static final ResourceLocation ID = RL("cow");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        // mooshrooms are also a type of CowEntity so don't use instanceof here
        return entity.getType() == EntityType.COW;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.fungiInfuse"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.fungiInfusion"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 100;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (!entity.level.isClientSide) {
            entity.discard();
            MushroomCow entitycow = new MushroomCow(EntityType.MOOSHROOM, entity.level);
            entitycow.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            entitycow.setHealth(((Cow) entity).getHealth());
            entitycow.yBodyRot = ((Cow) entity).yBodyRot;
            entity.level.addFreshEntity(entitycow);
            entity.level.addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0F, entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
