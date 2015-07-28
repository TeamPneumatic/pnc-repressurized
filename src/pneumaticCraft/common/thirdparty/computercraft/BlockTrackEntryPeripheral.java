package pneumaticCraft.common.thirdparty.computercraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryPeripheral implements IBlockTrackEntry{
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block, TileEntity te){
        if(block instanceof IPeripheralProvider) {
            IPeripheral peripheral = ((IPeripheralProvider)block).getPeripheral(te.getWorldObj(), x, y, z, 0);
            return peripheral != null && peripheral.getMethodNames().length != 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te){
        return false;
    }

    @Override
    public int spamThreshold(){
        return 8;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, TileEntity te, List<String> infoList){
        infoList.add("blockTracker.info.peripheral.title");
        infoList.add("blockTracker.info.peripheral.availableMethods");
        IPeripheral peripheral = ((IPeripheralProvider)world.getBlock(x, y, z)).getPeripheral(world, x, y, z, 0);
        if(peripheral != null) {
            for(String method : peripheral.getMethodNames()) {
                infoList.add("-" + method);
            }
        }
    }

    @Override
    public String getEntryName(){
        return "blockTracker.module.peripheral";
    }
}
