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

package me.desht.pneumaticcraft.datagen.loot;

import net.minecraft.loot.LootFunctionType;
import net.minecraft.util.registry.Registry;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModLootFunctions {
    public static final LootFunctionType TE_SERIALIZER = Registry.register(Registry.LOOT_FUNCTION_TYPE,
            RL("te_serializer"), new LootFunctionType(new TileEntitySerializerFunction.Serializer())
    );

    @SuppressWarnings("EmptyMethod")
    public static void init() {
        // poke
    }
}
