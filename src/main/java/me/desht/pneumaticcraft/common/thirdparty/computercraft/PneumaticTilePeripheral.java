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

package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import me.desht.pneumaticcraft.common.block.entity.ILuaMethodProvider;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CopyOnWriteArrayList;

public class PneumaticTilePeripheral implements IDynamicPeripheral, ComputerEventManager.IComputerEventSender {
    public static final Capability<IPeripheral> PERIPHERAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    private final ILuaMethodProvider provider;
    private final CopyOnWriteArrayList<IComputerAccess> attachedComputers = new CopyOnWriteArrayList<>();

    PneumaticTilePeripheral(ILuaMethodProvider provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return provider.getLuaMethodRegistry().getMethodNames();
    }

    @Nonnull
    @Override
    public MethodResult callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext ctx, int method, @Nonnull IArguments args) throws LuaException {
        try {
            return MethodResult.of(provider.getLuaMethodRegistry().getMethod(method).call(args.getAll()));
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Nonnull
    @Override
    public String getType() {
        return provider.getPeripheralType();
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        // TODO verify this is sufficient
        return this == other;
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        attachedComputers.add(computer);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        attachedComputers.remove(computer);
    }

    @Override
    public void sendEvent(BlockEntity te, String name, Object... params) {
        attachedComputers.forEach(a -> a.queueEvent(name, params));
    }
}
