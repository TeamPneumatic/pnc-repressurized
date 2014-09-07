package pneumaticCraft.client.render.pneumaticArmor.hacking.block;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;

public class HackableMobSpawner implements IHackableBlock{
    @Override
    public String getId(){
        return "mobSpawner";
    }

    @Override
    public boolean canHack(IBlockAccess world, int x, int y, int z, EntityPlayer player){
        return !isHacked(world, x, y, z);
    }

    public static boolean isHacked(IBlockAccess world, int x, int y, int z){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToNBT(tag);
            if(tag.hasKey("RequiredPlayerRange") && tag.getShort("RequiredPlayerRange") == 0) return true;
        }
        return false;
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
        return 200;
    }

    @Override
    public void onHackFinished(World world, int x, int y, int z, EntityPlayer player){
        if(!world.isRemote) {
            NBTTagCompound tag = new NBTTagCompound();
            TileEntity te = world.getTileEntity(x, y, z);
            te.writeToNBT(tag);
            tag.setShort("RequiredPlayerRange", (short)0);
            te.readFromNBT(tag);
            world.markBlockForUpdate(x, y, z);
        }

    }

    @Override
    public boolean afterHackTick(World world, int x, int y, int z){
        MobSpawnerBaseLogic spawner = ((TileEntityMobSpawner)world.getTileEntity(x, y, z)).func_145881_a();
        spawner.field_98284_d = spawner.field_98287_c;//oldRotation = rotation, to stop render glitching
        spawner.spawnDelay = 10;
        return false;
    }
}
