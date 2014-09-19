package pneumaticCraft.client.render.pneumaticArmor.hacking.block;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

public class HackableSecurityStation implements IHackableBlock{
    @Override
    public String getId(){
        return null;
    }

    @Override
    public boolean canHack(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        TileEntitySecurityStation te = (TileEntitySecurityStation)world.getTileEntity(x, y, z);
        return !te.doesAllowPlayer(player);
    }

    @Override
    public void addInfo(World world, int x, int y, int z, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.result.access");
    }

    @Override
    public void addPostHackInfo(World world, int x, int y, int z, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.accessed");
    }

    @Override
    public int getHackTime(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        return 100;
    }

    @Override
    public void onHackFinished(World world, int x, int y, int z, EntityPlayer player){
        world.getBlock(x, y, z).onBlockActivated(world, x, y, z, player, 0, 0, 0, 0);
    }

    @Override
    public boolean afterHackTick(World world, int x, int y, int z){
        return false;
    }
}
