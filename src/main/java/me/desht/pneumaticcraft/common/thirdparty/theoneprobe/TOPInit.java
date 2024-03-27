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

package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.*;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.IPneumaticCraftProbeable;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.function.Function;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TOPInit implements Function<ITheOneProbe, Void> {
    static final ResourceLocation ELEMENT_PRESSURE = RL("pressure");
    private static final ChatFormatting COLOR = ChatFormatting.GRAY;


    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        Log.info("Enabled support for The One Probe");

        theOneProbe.registerElementFactory(new ElementPressure.Factory());

        theOneProbe.registerProvider(new IProbeInfoProvider() {
            @Override
            public ResourceLocation getID() {
                return RL("default");
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
                if (blockState.getBlock() instanceof IPneumaticCraftProbeable || blockState.is(PneumaticCraftTags.Blocks.PROBE_TARGET)) {
                    TOPInfoProvider.handleBlock(mode, probeInfo, player, world, data);
                }
                SemiblockTracker.getInstance().getAllSemiblocks(world, data.getPos(), data.getSideHit())
                        .filter(sb -> !(sb instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) sb).getSide() == data.getSideHit())
                        .forEach(sb -> TOPInfoProvider.handleSemiblock(player, mode, probeInfo, sb));
            }
        });

        theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
            @Override
            public String getID() {
                return Names.MOD_ID + ":entity";
            }

            @Override
            public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, Entity entity, IProbeHitEntityData data) {
                if (entity instanceof ISemiBlock semiblock) {
                    CompoundTag tag = semiblock.serializeNBT(new CompoundTag());
                    semiblock.addTooltip(probeInfo::text, player, tag, player.isShiftKeyDown());
                    BlockPos pos = semiblock.getBlockPos();
                    BlockState state = world.getBlockState(pos);
                    if (!state.isAir()) {
                        IProbeInfo h = probeInfo.horizontal();
                        h.item(new ItemStack(state.getBlock()));
                        IProbeInfo v = h.vertical();
                        Component text = Component.translatable(state.getBlock().getDescriptionId());
                        v.text(text.copy().withStyle(ChatFormatting.YELLOW));
                        v.text(Component.literal(ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + ModNameCache.getModName(state.getBlock())));
                    }
                }
                IOHelper.getCapV(entity, PNCCapabilities.AIR_HANDLER_ENTITY).ifPresent(h -> {
                    String p = PneumaticCraftUtils.roundNumberTo(h.getPressure(), 1);
                    probeInfo.text(xlate("pneumaticcraft.gui.tooltip.pressure", p).withStyle(COLOR));
                });
                IOHelper.getCap(entity, Capabilities.FluidHandler.ENTITY)
                        .ifPresent(h -> TOPInfoProvider.handleFluidTanks(mode, probeInfo, h));
            }
        });
        return null;
    }
}
