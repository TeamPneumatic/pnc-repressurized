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

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.AbstractPersistentEntityHack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableGuardian extends AbstractPersistentEntityHack<Guardian> {
    private static final ResourceLocation ID = RL("guardian");

    public HackableGuardian() {
        super(StockHackTypes.DISARM);
    }

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Guardian> getHackableClass() {
        return Guardian.class;
    }

    @Override
    public void onHackFinished(Guardian entity, Player player) {
        entity.setTarget(null);

        if (entity instanceof ElderGuardian) {
            player.removeEffectNoUpdate(MobEffects.DIG_SLOWDOWN);
        }
    }

    @Override
    public boolean afterHackTick(Guardian entity) {
        entity.setTarget(null);
        return true;
    }
}
