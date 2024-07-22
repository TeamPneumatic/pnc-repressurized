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
import dan200.computercraft.api.peripheral.PeripheralCapability;
import me.desht.pneumaticcraft.common.block.entity.ILuaMethodProvider;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class PneumaticTilePeripheral implements IDynamicPeripheral, ComputerEventManager.IComputerEventSender {
    private final ILuaMethodProvider provider;
    private final CopyOnWriteArrayList<IComputerAccess> attachedComputers = new CopyOnWriteArrayList<>();

    private PneumaticTilePeripheral(ILuaMethodProvider provider) {
        this.provider = provider;
    }

    private static PneumaticTilePeripheral maybe(Object o, Direction dir) {
        return o instanceof ILuaMethodProvider p ? new PneumaticTilePeripheral(p) : null;
    }

    static void attachPeripheralCap(RegisterCapabilitiesEvent event) {
        if (ComputerCraft.available) {
            ModBlockEntityTypes.streamBlockEntities().forEach(blockEntity -> {
                if (blockEntity instanceof ILuaMethodProvider) {
                    event.registerBlockEntity(PeripheralCapability.get(), blockEntity.getType(), PneumaticTilePeripheral::maybe);
                }
            });
        }
    }

    static Optional<IPeripheral> getPeripheral(BlockEntity blockEntity) {
        return Optional.ofNullable(blockEntity.getLevel().getCapability(PeripheralCapability.get(),
                blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity,  Direction.UP)); // direction doesn't matter
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
        return other != null && this.getTarget() == other.getTarget();
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

    @Override
    public Object getTarget() {
        return provider;
    }
}
