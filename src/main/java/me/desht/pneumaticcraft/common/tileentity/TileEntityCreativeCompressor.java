package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileEntityCreativeCompressor extends TileEntityPneumaticBase implements INamedContainerProvider {
    @GuiSynced
    private float pressureSetpoint;

    public TileEntityCreativeCompressor() {
        super(ModTileEntities.CREATIVE_COMPRESSOR.get(), 30, 30, 50000, 0);
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        pressureSetpoint = tag.getFloat("setpoint");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putFloat("setpoint", pressureSetpoint);
        return nbt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote) {
            airHandler.setPressure(pressureSetpoint);
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        try {
            pressureSetpoint += Float.parseFloat(tag);
            if (pressureSetpoint > 30) pressureSetpoint = 30;
            if (pressureSetpoint < -1) pressureSetpoint = -1;
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerCreativeCompressor(i, playerInventory, getPos());
    }

}
