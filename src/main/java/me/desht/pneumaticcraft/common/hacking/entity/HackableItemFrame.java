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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableItemFrame implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("item_frame");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof ItemFrameEntity && !((ItemFrameEntity) entity).getItem().isEmpty();
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Hack to detach item"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Item detached!"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.level.isClientSide) {
            entity.hurt(DamageSource.playerAttack(player), 0.1f);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
