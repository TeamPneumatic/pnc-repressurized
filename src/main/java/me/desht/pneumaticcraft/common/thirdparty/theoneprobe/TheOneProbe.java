package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.*;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TheOneProbe implements IThirdParty {

    private static final TextFormatting COLOR = TextFormatting.GRAY;
    static int elementPressure;

    @Override
    public void init() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) theOneProbe -> {
            Log.info("Enabled support for The One Probe");

            elementPressure = theOneProbe.registerElementFactory(ElementPressure::new);

            theOneProbe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return Names.MOD_ID + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
                    if (blockState.getBlock() instanceof BlockPneumaticCraft) {
                        TOPInfoProvider.handle(mode, probeInfo, player, world, blockState, data);
                    }
                    SemiblockTracker.getInstance().getAllSemiblocks(world, data.getPos())
                            .forEach(semiBlock -> TOPInfoProvider.handleSemiblock(player, mode, probeInfo, semiBlock));
                }
            });

            theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
                @Override
                public String getID() {
                    return Names.MOD_ID + ":entity";
                }

                @Override
                public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data) {
                    if (entity instanceof ISemiBlock) {
                        List<ITextComponent> tip = new ArrayList<>();
                        CompoundNBT tag = ((ISemiBlock) entity).serializeNBT(new CompoundNBT());
                        ((ISemiBlock) entity).addTooltip(tip, player, tag, player.isSneaking());
                        tip.forEach(text -> probeInfo.text(text.getFormattedText()));
                    }
                    entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY).ifPresent(h -> {
                        String p = PneumaticCraftUtils.roundNumberTo(h.getPressure(), 1);
                        probeInfo.text(COLOR + "Pressure: " + p + " bar");
                    });
                }
            });
            return null;
        });
    }



}
