package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.pressure.AirHandlerMachine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class TileEntityCreativeCompressor extends TileEntityPneumaticBase implements INamedContainerProvider {
    @GuiSynced
    private float pressureSetpoint;

    public TileEntityCreativeCompressor() {
        super(ModTileEntityTypes.CREATIVE_COMPRESSOR, 30, 30, 50000, 0);
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        pressureSetpoint = nbt.getFloat("setpoint");
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
            ((AirHandlerMachine) getAirHandler(null)).setPressure(pressureSetpoint);
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        try {
            pressureSetpoint += Float.parseFloat(tag);
            if (pressureSetpoint > 30) pressureSetpoint = 30;
            if (pressureSetpoint < -1) pressureSetpoint = -1;
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerCreativeCompressor(i, playerInventory, getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }
}
