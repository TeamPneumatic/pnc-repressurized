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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableCaveSpider implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("cave_spider");
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.neutralize"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.neutralized"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 50;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (!entity.level.isClientSide) {
            entity.discard();
            Spider spider = new Spider(EntityType.SPIDER, entity.level);
            spider.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            spider.setHealth(((Spider) entity).getHealth());
            spider.yBodyRot = ((Spider) entity).yBodyRot;
            entity.level.addFreshEntity(spider);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
