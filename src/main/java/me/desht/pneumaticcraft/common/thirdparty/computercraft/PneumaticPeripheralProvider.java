package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import me.desht.pneumaticcraft.common.tileentity.ILuaMethodProvider;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PneumaticPeripheralProvider implements ICapabilityProvider {
    private final IPeripheral impl;
    private final LazyOptional<IPeripheral> lazy;

    PneumaticPeripheralProvider(ILuaMethodProvider luaMethodProvider) {
        impl = new PneumaticTilePeripheral(luaMethodProvider);
        lazy = LazyOptional.of(() -> impl);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ComputerCraft.PERIPHERAL_CAPABILITY.orEmpty(cap, lazy);
    }
}
