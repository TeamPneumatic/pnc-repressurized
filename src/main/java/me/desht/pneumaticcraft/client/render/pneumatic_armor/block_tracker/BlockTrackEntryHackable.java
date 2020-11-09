package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryHackable implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return HackClientHandler.enabledForPlayer(ClientUtils.getClientPlayer())
                && HackableHandler.getHackableForBlock(world, pos, ClientUtils.getClientPlayer()) != null;
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
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList) {
        PlayerEntity player = ClientUtils.getClientPlayer();
        IHackableBlock hackableBlock = HackableHandler.getHackableForBlock(world, pos, player);
        assert hackableBlock != null;
        int hackTime = ArmorUpgradeClientRegistry.getInstance().byClass(BlockTrackerClientHandler.class).getTargetForCoord(pos).getHackTime();
        if (hackTime == 0) {
            hackableBlock.addInfo(world, pos, infoList, player);
            HackClientHandler.addKeybindTooltip(infoList);
        } else {
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            int percentageComplete = hackTime * 100 / requiredHackTime;
            if (percentageComplete < 100) {
                infoList.add(xlate("pneumaticcraft.armor.hacking.hacking", percentageComplete));
            } else if (hackTime < requiredHackTime + 20) {
                hackableBlock.addPostHackInfo(world, pos, infoList, player);
            } else {
                hackableBlock.addInfo(world, pos, infoList, player);
                HackClientHandler.addKeybindTooltip(infoList);
            }
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return RL("block_tracker.module.hackables");
    }
}
