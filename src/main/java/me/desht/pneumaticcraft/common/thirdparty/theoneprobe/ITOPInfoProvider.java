package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface ITOPInfoProvider {
    void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data);
}
