package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

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
        if (!world.isRemote && stack.getMaxDamage() - stack.getDamage() >= 100) {
            List<ISemiBlock> semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(world, pos);
            
            if(semiBlocks.isEmpty()){
                pos = pos.offset(side);
                semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(world, pos);
            }

            if (!semiBlocks.isEmpty()) {
                if (player.isSneaking()) {
                    for (ISemiBlock s : semiBlocks) {
                        if (!(s instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) s).getFacing() == side) {
                            SemiBlockManager.getInstance(world).breakSemiBlock(s, player);
                        }
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    //TODO raytrace?
                    if (semiBlocks.stream().anyMatch(s -> s.onRightClickWithConfigurator(player, side))) {
                        stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                                .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        } else if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
