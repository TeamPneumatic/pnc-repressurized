/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.create;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Create implements IThirdParty {
    @Override
    public void init() {
//        PneumaticRegistry.getInstance().getWrenchRegistry().addModdedWrenchBehaviour(ModIds.CREATE,
//                (ctx, state) -> {
//                    // pre
//                    if (state.getBlock() instanceof BeltBlock) {
//                        HitResult rtr = RayTraceUtils.getMouseOverServer(ctx.getPlayer(), PneumaticCraftUtils.getPlayerReachDistance(ctx.getPlayer()));
//                        if (rtr instanceof BlockHitResult) {
//                            return BeltSlicer.useWrench(state, ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer(), ctx.getHand(),
//                                    (BlockHitResult) rtr, new BeltSlicer.Feedback());
//                        }
//                    } else if (state.getBlock() instanceof IWrenchable) {
//                        return ctx.getPlayer() != null && ctx.getPlayer().isCrouching() ?
//                                ((IWrenchable) state.getBlock()).onSneakWrenched(state, ctx) :
//                                ((IWrenchable) state.getBlock()).onWrenched(state, ctx);
//                    }
//                    return InteractionResult.PASS;
//                },
//                (ctx, state) -> {
//                    // post
//                }
//        );
    }
}
