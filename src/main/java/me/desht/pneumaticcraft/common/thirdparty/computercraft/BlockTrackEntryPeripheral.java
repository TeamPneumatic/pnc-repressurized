package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryPeripheral implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        if (te instanceof IPeripheral) {
            IPeripheral peripheral = (IPeripheral) te;
            return peripheral.getMethodNames().length > 0;
        }
        return false;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        infoList.add("blockTracker.info.peripheral.title");
        infoList.add("blockTracker.info.peripheral.availableMethods");
        IPeripheral peripheral = (IPeripheral) te;
        if (peripheral != null) {
            for (String method : peripheral.getMethodNames()) {
                infoList.add("\u2022 " + method);
            }
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.peripheral";
    }
}
