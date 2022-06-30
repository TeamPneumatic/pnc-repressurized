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
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableSheep implements IHackableEntity<Sheep> {
    private static final ResourceLocation ID = RL("sheep");

    @Nullable
    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Sheep> getHackableClass() {
        return Sheep.class;
    }

    @Override
    public void addHackInfo(Sheep entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.changeColor"));
    }

    @Override
    public void addPostHackInfo(Sheep entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.changeColor"));
    }

    @Override
    public int getHackTime(Sheep entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Sheep entity, Player player) {
        DyeColor newColor = DyeColor.byId(player.getRandom().nextInt(DyeColor.values().length));
        entity.setColor(newColor);
    }
}
