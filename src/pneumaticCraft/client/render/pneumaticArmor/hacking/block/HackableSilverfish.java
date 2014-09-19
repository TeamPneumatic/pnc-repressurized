package pneumaticCraft.client.render.pneumaticArmor.hacking.block;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;

public class HackableSilverfish implements IHackableBlock{

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
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(World world, int x, int y, int z, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        return 40;
    }

    @Override
    public void onHackFinished(World world, int x, int y, int z, EntityPlayer player){
        world.setBlock(x, y, z, Blocks.stone);
    }

    @Override
    public boolean afterHackTick(World world, int x, int y, int z){
        return false;
    }

}
