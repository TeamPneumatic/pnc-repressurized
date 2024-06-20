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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModHoeHandlers {
    public static final DeferredRegister<HoeHandler> HOE_HANDLERS_DEFERRED
            = DeferredRegister.create(PNCRegistries.HOE_HANDLER_REGISTRY, Names.MOD_ID);

    public static final Supplier<HoeHandler> DEFAULT = HOE_HANDLERS_DEFERRED.register("default_hoe_handler", HoeHandler.DefaultHoeHandler::new);
}
