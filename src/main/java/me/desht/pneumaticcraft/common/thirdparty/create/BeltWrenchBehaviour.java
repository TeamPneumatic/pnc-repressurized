package me.desht.pneumaticcraft.common.thirdparty.create;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.BiFunction;

public class BeltWrenchBehaviour implements BiFunction<UseOnContext, BlockState, InteractionResult> {
    @Override
    public InteractionResult apply(UseOnContext ctx, BlockState state) {
        if (state.getBlock() instanceof BeltBlock) {
            HitResult rtr = RayTraceUtils.getMouseOverServer(ctx.getPlayer(), PneumaticCraftUtils.getPlayerReachDistance(ctx.getPlayer()));
            if (rtr instanceof BlockHitResult brtr) {
                return BeltSlicer.useWrench(state, ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer(), ctx.getHand(),
                        brtr, new BeltSlicer.Feedback());
            }
        } else if (state.getBlock() instanceof IWrenchable wrenchable) {
            return ctx.getPlayer() != null && ctx.getPlayer().isCrouching() ?
                    wrenchable.onSneakWrenched(state, ctx) :
                    wrenchable.onWrenched(state, ctx);
        }
        return InteractionResult.PASS;
    }
}
