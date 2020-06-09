package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ItemSeismicSensor extends Item {
    public ItemSeismicSensor() {
        super(ModItems.defaultProps().maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        PlayerEntity player = ctx.getPlayer();
        if (!world.isRemote) {
            int startY = pos.getY();
            while (pos.getY() > 0) {
                pos = pos.offset(Direction.DOWN);
                if (isOil(world, pos)) {
                    Set<BlockPos> oilPositions = new HashSet<>();
                    Stack<BlockPos> pendingPositions = new Stack<>();
                    pendingPositions.add(new BlockPos(pos));
                    while (!pendingPositions.empty()) {
                        BlockPos checkingPos = pendingPositions.pop();
                        for (Direction d : Direction.values()) {
                            BlockPos newPos = checkingPos.offset(d);
                            if (isOil(world, newPos) && oilPositions.add(newPos)) {
                                pendingPositions.add(newPos);
                            }
                        }
                    }
                    player.sendStatusMessage(new TranslationTextComponent(
                            "pneumaticcraft.message.seismicSensor.foundOilDetails",
                            TextFormatting.GREEN.toString() + (startY - pos.getY()),
                            TextFormatting.GREEN.toString() + oilPositions.size() / 10 * 10),
                            false);
                    return ActionResultType.SUCCESS;
                }
            }
            player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.seismicSensor.noOilFound"), false);
        }
        return ActionResultType.SUCCESS; // we don't want to use the item.

    }

    private boolean isOil(World world, BlockPos pos) {
        // TODO 1.14 make more tag-friendly
        return world.getFluidState(pos).getFluid() == ModFluids.OIL.get();
    }
}
