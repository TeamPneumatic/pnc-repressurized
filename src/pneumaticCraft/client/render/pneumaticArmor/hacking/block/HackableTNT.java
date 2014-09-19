package pneumaticCraft.client.render.pneumaticArmor.hacking.block;

import java.util.List;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;

public class HackableTNT implements IHackableBlock{
    @Override
    public String getId(){
        return null;
    }

    @Override
    public boolean canHack(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        return true;
    }

    @Override
    public void addInfo(World world, int x, int y, int z, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.result.explode");
    }

    @Override
    public void addPostHackInfo(World world, int x, int y, int z, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.exploded");
    }

    @Override
    public int getHackTime(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        return 100;
    }

    @Override
    public void onHackFinished(World world, int x, int y, int z, EntityPlayer player){
        if(!world.isRemote) {
            world.setBlockToAir(x, y, z);
            EntityTNTPrimed tnt = new EntityTNTPrimed(world, x + 0.5, y + 0.5, z + 0.5, player);
            tnt.fuse = 1;
            world.spawnEntityInWorld(tnt);
        }
    }

    @Override
    public boolean afterHackTick(World world, int x, int y, int z){
        return false;
    }

}