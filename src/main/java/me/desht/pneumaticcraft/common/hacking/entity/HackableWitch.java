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
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableWitch implements IHackableEntity<Witch> {
    private static final ResourceLocation ID = RL("witch");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Witch> getHackableClass() {
        return Witch.class;
    }

    @Override
    public void addHackInfo(Witch entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.disarm"));
    }

    @Override
    public void addPostHackInfo(Witch entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.disarmed"));
    }

    @Override
    public int getHackTime(Witch entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Witch entity, Player player) {
        // nothing here, see afterHackTick logic
    }

    @Override
    public boolean afterHackTick(Witch entity) {
        entity.usingTime = 20;
        entity.setUsingItem(true);
        return true;
    }
}
