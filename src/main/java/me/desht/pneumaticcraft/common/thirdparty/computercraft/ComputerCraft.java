package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ComputerCraft extends OpenComputers {
    @Override
    public void preInit() {
        ThirdPartyManager.computerCraftLoaded = true;
        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryPeripheral());
        super.preInit();
    }

    @Override
    public void init() {
        ComputerCraftAPI.registerPeripheralProvider(new IPeripheralProvider() {
            @Nullable
            @Override
            public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing enumFacing) {
                TileEntity te = world.getTileEntity(blockPos);
                return te != null && te.getClass().getName().startsWith("me.desht.pneumaticcraft")
                        && te instanceof IPeripheral ? (IPeripheral) te : null;
            }
        });

        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) super.init();
    }
}
