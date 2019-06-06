package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackUpgradeHandler;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrackEntryHackable implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        return HackUpgradeHandler.enabledForPlayer(PneumaticCraftRepressurized.proxy.getClientPlayer()) && HackableHandler.getHackableForCoord(world, pos, PneumaticCraftRepressurized.proxy.getClientPlayer()) != null;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(world, pos, PneumaticCraftRepressurized.proxy.getClientPlayer());
        int hackTime = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(pos).getHackTime();
        if (hackTime == 0) {
            hackableBlock.addInfo(world, pos, infoList, PneumaticCraftRepressurized.proxy.getClientPlayer());
            HackUpgradeHandler.addKeybindTooltip(infoList);
        } else {
            int requiredHackTime = hackableBlock.getHackTime(world, pos, PneumaticCraftRepressurized.proxy.getClientPlayer());
            int percentageComplete = hackTime * 100 / requiredHackTime;
            if (percentageComplete < 100) {
                infoList.add(I18n.format("pneumaticHelmet.hacking.hacking", percentageComplete));
            } else if (hackTime < requiredHackTime + 20) {
                hackableBlock.addPostHackInfo(world, pos, infoList, PneumaticCraftRepressurized.proxy.getClientPlayer());
            } else {
                hackableBlock.addInfo(world, pos, infoList, PneumaticCraftRepressurized.proxy.getClientPlayer());
                HackUpgradeHandler.addKeybindTooltip(infoList);
            }
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.hackables";
    }

}
