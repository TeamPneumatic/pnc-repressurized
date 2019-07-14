package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryPeripheral implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, BlockState state, TileEntity te) {
        if (te instanceof IPeripheral) {
            IPeripheral peripheral = (IPeripheral) te;
            return peripheral.getMethodNames().length > 0;
        }
        return false;
    }

    public boolean getServerUpdatePositions(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
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
