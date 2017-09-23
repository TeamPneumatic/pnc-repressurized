package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ItemSeismicSensor extends ItemPneumatic {
    public ItemSeismicSensor() {
        super("seismic_sensor");
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            int startY = pos.getY();
            while (pos.getY() > 0) {
                pos = pos.offset(EnumFacing.DOWN);
                if (world.getBlockState(pos).getBlock() == FluidRegistry.getFluid(Fluids.OIL.getName()).getBlock()) {
                    Set<BlockPos> oilPositions = new HashSet<>();
                    Stack<BlockPos> pendingPositions = new Stack<>();
                    pendingPositions.add(new BlockPos(pos));
                    while (!pendingPositions.empty()) {
                        BlockPos checkingPos = pendingPositions.pop();
                        for (EnumFacing d : EnumFacing.VALUES) {
                            BlockPos newPos = checkingPos.offset(d);
                            if (world.getBlockState(newPos).getBlock() == Fluids.OIL.getBlock() && FluidUtils.isSourceBlock(world, newPos) && oilPositions.add(newPos)) {
                                pendingPositions.add(newPos);
                            }
                        }
                    }
                    player.sendStatusMessage(new TextComponentTranslation(
                            "message.seismicSensor.foundOilDetails",
                            TextFormatting.GREEN.toString() + (startY - pos.getY()),
                            TextFormatting.GREEN.toString() + oilPositions.size() / 10 * 10),
                            false);
                    return EnumActionResult.SUCCESS;
                }
            }
            player.sendStatusMessage(new TextComponentTranslation("message.seismicSensor.noOilFound"), false);
        }
        return EnumActionResult.SUCCESS; // we don't want to use the item.

    }
}
