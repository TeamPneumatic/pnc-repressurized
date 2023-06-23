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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableItemFrame implements IHackableEntity<ItemFrame> {

    private static final ResourceLocation ID = RL("item_frame");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<ItemFrame> getHackableClass() {
        return ItemFrame.class;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return IHackableEntity.super.canHack(entity, player)
                && entity instanceof ItemFrame f && !f.getItem().isEmpty();
    }

    @Override
    public void addHackInfo(ItemFrame entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Hack to detach item"));
    }

    @Override
    public void addPostHackInfo(ItemFrame entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Item detached!"));
    }

    @Override
    public int getHackTime(ItemFrame entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(ItemFrame entity, Player player) {
        if (!entity.level.isClientSide) {
            entity.hurt(entity.damageSources().playerAttack(player), 0.1f);
        }
    }
}
