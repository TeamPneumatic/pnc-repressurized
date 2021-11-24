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

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ModHoeHandlers {
    public static final DeferredRegister<HoeHandler> HOE_HANDLERS_DEFERRED = DeferredRegister.create(HoeHandler.class, Names.MOD_ID);
    public static final Supplier<IForgeRegistry<HoeHandler>> HOE_HANDLERS = HOE_HANDLERS_DEFERRED
            .makeRegistry("hoe_handlers", () -> new RegistryBuilder<HoeHandler>().disableSaving().disableSync());

    public static final RegistryObject<HoeHandler> DEFAULT = HOE_HANDLERS_DEFERRED.register("default_hoe_handler", HoeHandler.DefaultHoeHandler::new);
}
