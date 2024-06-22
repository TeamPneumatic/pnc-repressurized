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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackablePainting implements IHackableEntity<Painting> {

    private static final ResourceLocation ID = RL("painting");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Painting> getHackableClass() {
        return Painting.class;
    }

    @Override
    public void addHackInfo(Painting entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Hack to change artwork"));
    }

    @Override
    public void addPostHackInfo(Painting entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("Artwork changed!"));
    }

    @Override
    public int getHackTime(Painting entity, Player player) {
        return 40;
    }

    @Override
    public void onHackFinished(Painting entity, Player player) {
        PaintingVariant art = entity.getVariant().value();

        var candidates = entity.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).holders()
                .filter(h -> h.value().height() == art.height() && h.value().width() == art.width())
                .toList();
        entity.setVariant(candidates.get(entity.level().random.nextInt(candidates.size())));
    }
}
