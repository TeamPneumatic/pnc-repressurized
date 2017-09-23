package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class HackableTripwire implements IHackableBlock {

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return 40;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, EntityPlayer player) {
        world.destroyBlock(pos, true);//break block
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}
