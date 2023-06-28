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
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableBat implements IHackableEntity<Bat> {
    private static final ResourceLocation ID = RL("bat");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public Class<Bat> getHackableClass() {
        return Bat.class;
    }

    @Override
    public void addHackInfo(Bat entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.kill"));
    }

    @Override
    public void addPostHackInfo(Bat entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.killed"));
    }

    @Override
    public int getHackTime(Bat entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Bat entity, Player player) {
        if (!entity.level().isClientSide) {
            entity.discard();
            entity.level().explode(null, entity.getX(), entity.getY(), entity.getZ(), 0, Level.ExplosionInteraction.NONE);
        }
    }
}
