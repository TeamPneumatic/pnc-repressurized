package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class ItemLogisticsConfigurator extends ItemPressurizable {

    public ItemLogisticsConfigurator() {
        super(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        Direction side = ctx.getFace();

        if (!world.isRemote
                && stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> h.getPressure() > 0.1).orElseThrow(RuntimeException::new)) {
            Stream<ISemiBlock> semiBlocks = SemiblockTracker.getInstance().getAllSemiblocks(world, pos, side);

            if (player.isSneaking()) {
                semiBlocks.filter(s -> !(s instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) s).getSide() == side)
                        .forEach(s -> s.removeSemiblock(player));
                return ActionResultType.SUCCESS;
            } else {
                if (semiBlocks.anyMatch(s -> s.onRightClickWithConfigurator(player, side))) {
                    stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                    return ActionResultType.SUCCESS;
                }
            }
        } else if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
