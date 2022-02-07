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
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackablePainting implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("painting");
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Hack to change artwork"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Artwork changed!"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 40;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        Motive art = ((Painting) entity).motive;
        List<Motive> candidate = new ArrayList<>();
        for (Motive a : ForgeRegistries.PAINTING_TYPES.getValues()) {
            if (a.getHeight() == art.getHeight() && a.getWidth() == art.getWidth()) {
                candidate.add(a);
            }
        }
        ((Painting) entity).motive = candidate.get(entity.level.random.nextInt(candidate.size()));
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
