package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.IGlobalVariableHelper;
import me.desht.pneumaticcraft.api.misc.IMiscHelpers;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.common.block.entity.utility.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketNotifyBlockUpdate;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.Validate;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

public enum MiscAPIHandler implements IMiscHelpers {
    INSTANCE;

    private static final Vector3f VEC3F_111 = new Vector3f(1, 1, 1);

    public static MiscAPIHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public int getProtectingSecurityStations(Player player, BlockPos pos) {
        Validate.isTrue(!player.getCommandSenderWorld().isClientSide, "This method can only be called from the server side!");
        return SecurityStationBlockEntity.getProtectingSecurityStations(player, pos, false);
    }

    @Override
    public void registerXPFluid(FluidIngredient tag, int liquidToPointRatio) {
        XPFluidManager.getInstance().registerXPFluid(tag, liquidToPointRatio);
    }

    @Override
    public void syncGlobalVariable(ServerPlayer player, String varName) {
        BlockPos pos = getGlobalVariableHelper().getPos(player.getUUID(), varName);
        NetworkHandler.sendToPlayer(PacketSetGlobalVariable.forPos(varName, pos), player);
        // TODO should we sync item variables too?
        //  right now there isn't really a need for it, so it would just be extra network chatter
    }

    @Override
    public IItemHandler deserializeSmartChest(CompoundTag tag, HolderLookup.Provider provider) {
        return SmartChestBlockEntity.deserializeSmartChest(tag, provider);
    }

    @Override
    public void forceClientShapeRecalculation(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            NetworkHandler.sendToAllTracking(new PacketNotifyBlockUpdate(pos), world, pos);
        }
    }

    @Override
    public void playMachineBreakEffect(BlockEntity blockEntity) {
        Level level = Objects.requireNonNull(blockEntity.getLevel());
        BlockPos pos = blockEntity.getBlockPos();
        PNCCapabilities.getAirHandler(blockEntity).ifPresent(handler -> {
            if (handler.getAir() > 0) {
                NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE,
                        new Vector3f(pos.getX(), pos.getY(), pos.getZ()),
                        PneumaticCraftUtils.VEC3F_ZERO,
                        (int) (5 * handler.getPressure()),
                        Optional.of(VEC3F_111)
                ), level, pos);
                level.playSound(null, pos, ModSounds.SHORT_HISS.get(), SoundSource.BLOCKS, 0.3f, 0.8f);
            }
        });
    }

    @Override
    public ParticleOptions airParticle() {
        return AirParticleData.DENSE;
    }

    @Override
    public Optional<? extends IActiveEntityHacks> getHackingForEntity(Entity entity, boolean create) {
        return create ? HackManager.getOrCreateActiveHacks(entity) : HackManager.getActiveHacks(entity);
    }

    @Override
    public IGlobalVariableHelper getGlobalVariableHelper() {
        return GlobalVariableHelper.getInstance();
    }
}
