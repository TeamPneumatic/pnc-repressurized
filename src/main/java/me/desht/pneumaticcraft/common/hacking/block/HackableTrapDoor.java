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

package me.desht.pneumaticcraft.common.hacking.block;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableTrapDoor extends HackableDoor {
    private static final Identifier ID = RL("trapdoor");

    @Override
    public Identifier getHackableId() {
        return ID;
    }

    @Override
    protected BooleanProperty getOpenProperty() {
        return TrapDoorBlock.OPEN;
    }
}
