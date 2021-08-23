package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableDoor implements IHackableBlock {
    @Override
    public ResourceLocation getHackableId() {
        return RL("door");
    }

    @Override
    public boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return world.getBlockState(pos).hasProperty(getOpenProperty());
    }

    @Override
    public void addInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        if (world.getBlockState(pos).getValue(getOpenProperty())) {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.result.close"));
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.result.open"));
        }
    }

    @Override
    public void addPostHackInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        if (world.getBlockState(pos).getValue(getOpenProperty())) {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.opened"));
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.closed"));
        }
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 20;
    }

    @Override
    public void onHackComplete(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        fakeRayTrace(player, pos).ifPresent(rtr -> state.use(world, player, Hand.MAIN_HAND, rtr));
    }

    protected BooleanProperty getOpenProperty() {
        return DoorBlock.OPEN;
    }
}
