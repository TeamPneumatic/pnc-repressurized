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
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Hack to change artwork"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Artwork changed!"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 40;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        PaintingType art = ((PaintingEntity) entity).motive;
        List<PaintingType> candidate = new ArrayList<>();
        for (PaintingType a : ForgeRegistries.PAINTING_TYPES.getValues()) {
            if (a.getHeight() == art.getHeight() && a.getWidth() == art.getWidth()) {
                candidate.add(a);
            }
        }
        ((PaintingEntity) entity).motive = candidate.get(entity.level.random.nextInt(candidate.size()));
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
