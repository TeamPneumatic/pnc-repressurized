package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class TileEntityPressureChamberWall extends TileEntityBase implements IManoMeasurable, IInfoForwarder {

    private TileEntityPressureChamberValve teValve;
    private int valveX;
    private int valveY;
    private int valveZ;

    public TileEntityPressureChamberWall() {
        this(ModTileEntities.PRESSURE_CHAMBER_WALL.get(), 0);
    }

    TileEntityPressureChamberWall(TileEntityType type, int upgradeSize) {
        super(type, upgradeSize);
    }

    public TileEntityPressureChamberValve getCore() {
        if (teValve == null && (valveX != 0 || valveY != 0 || valveZ != 0)) {
            // when the saved TE equals null, check if we can
            // retrieve the TE from the NBT saved coords.
            TileEntity te = getLevel().getBlockEntity(new BlockPos(valveX, valveY, valveZ));
            setCore(te instanceof TileEntityPressureChamberValve ? (TileEntityPressureChamberValve) te : null);
        }
        return teValve;
    }

    public void onBlockBreak() {
        teValve = getCore();
        if (teValve != null) {
            teValve.onMultiBlockBreak();
        }
    }

    void setCore(TileEntityPressureChamberValve te) {
        if (!getLevel().isClientSide) {
            if (te != null) {
                valveX = te.getBlockPos().getX();
                valveY = te.getBlockPos().getY();
                valveZ = te.getBlockPos().getZ();
            } else {
                valveX = 0;
                valveY = 0;
                valveZ = 0;
            }
        }
        boolean hasChanged = teValve != te;
        teValve = te;
        if (hasChanged && !getLevel().isClientSide) {
            BlockState curState = getLevel().getBlockState(getBlockPos());
            if (curState.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get()) {
                BlockState newState = ModBlocks.PRESSURE_CHAMBER_WALL.get().updateState(curState, getLevel(), getBlockPos());
                getLevel().setBlock(getBlockPos(), newState, 2);
            }
        }
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        teValve = null;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        valveX = tag.getInt("valveX");
        valveY = tag.getInt("valveY");
        valveZ = tag.getInt("valveZ");
        teValve = null;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putInt("valveX", valveX);
        tag.putInt("valveY", valveY);
        tag.putInt("valveZ", valveZ);
        return tag;
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        if (getCore() != null) {
            teValve.airHandler.printManometerMessage(player, curInfo);
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public TileEntity getInfoTileEntity(){
        return getCore();
    }

}
