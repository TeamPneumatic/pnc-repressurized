package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressedIronBlock;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class TileEntityCreativeCompressedIronBlock extends TileEntityCompressedIronBlock implements INamedContainerProvider {
    @GuiSynced
    public int targetTemperature = -1;  // -1 = uninited

    public TileEntityCreativeCompressedIronBlock() {
        super(ModTileEntities.CREATIVE_COMPRESSED_IRON_BLOCK.get());

        heatExchanger.setThermalCapacity(1_000_000);
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (targetTemperature < 0) {
                targetTemperature = (int) heatExchanger.getAmbientTemperature();
            }

            heatExchanger.setTemperature(targetTemperature);
        }

        super.tick();
    }

    @Override
    public boolean shouldShowGuiHeatTab() {
        return false;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putInt("targetTemperature", targetTemperature);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        targetTemperature = tag.getInt("targetTemperature");
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        try {
            targetTemperature += Integer.parseInt(tag) * (shiftHeld ? 10 : 1);
            targetTemperature = MathHelper.clamp(targetTemperature, 0, 2273);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void setTargetTemperature(int temp) {
        this.targetTemperature = temp;
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerCreativeCompressedIronBlock(windowId, playerInventory, getBlockPos());
    }
}
