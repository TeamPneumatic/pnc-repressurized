package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BlockTrackEntryHackable implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return HackUpgradeHandler.enabledForPlayer(ClientUtils.getClientPlayer())
                && HackableHandler.getHackableForCoord(world, pos, ClientUtils.getClientPlayer()) != null;
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.emptyList();
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
        PlayerEntity player = ClientUtils.getClientPlayer();
        IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(world, pos, player);
        assert hackableBlock != null;
        int hackTime = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(pos).getHackTime();
        if (hackTime == 0) {
            hackableBlock.addInfo(world, pos, infoList, player);
            HackUpgradeHandler.addKeybindTooltip(infoList);
        } else {
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            int percentageComplete = hackTime * 100 / requiredHackTime;
            if (percentageComplete < 100) {
                infoList.add(I18n.format("pneumaticHelmet.hacking.hacking", percentageComplete));
            } else if (hackTime < requiredHackTime + 20) {
                hackableBlock.addPostHackInfo(world, pos, infoList, player);
            } else {
                hackableBlock.addInfo(world, pos, infoList, player);
                HackUpgradeHandler.addKeybindTooltip(infoList);
            }
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.hackables";
    }

}
