package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockElevatorCaller;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityElevatorCaller extends TileEntityTickableBase implements ICamouflageableTE {
    private ElevatorButton[] floors = new ElevatorButton[0];
    private int thisFloor;
    private boolean emittingRedstone;
    private boolean shouldUpdateNeighbors;
    @DescSynced
    @Nonnull
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;

    public TileEntityElevatorCaller() {
        super(ModTileEntities.ELEVATOR_CALLER.get());
    }

    public void setEmittingRedstone(boolean emittingRedstone) {
        if (emittingRedstone != this.emittingRedstone) {
            this.emittingRedstone = emittingRedstone;
            shouldUpdateNeighbors = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (shouldUpdateNeighbors) {
            updateNeighbours();
            shouldUpdateNeighbors = false;
        }
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    public boolean getEmittingRedstone() {
        return emittingRedstone;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        emittingRedstone = tag.getBoolean("emittingRedstone");
        thisFloor = tag.getInt("thisFloor");
        camoStack = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
        shouldUpdateNeighbors = tag.getBoolean("shouldUpdateNeighbors");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("emittingRedstone", emittingRedstone);
        tag.putInt("thisFloor", thisFloor);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        tag.putBoolean("shouldUpdateNeighbors", shouldUpdateNeighbors);
        return tag;
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        int floorAmount = tag.getInt("floors");
        floors = new ElevatorButton[floorAmount];
        for (int i = 0; i < floorAmount; i++) {
            floors[i] = new ElevatorButton(tag.getCompound("floor" + i));
        }
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        tag.putInt("floors", floors.length);
        for (ElevatorButton floor : floors) {
            tag.put("floor" + floor.floorNumber, floor.writeToNBT(new CompoundNBT()));
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        boolean wasPowered = poweredRedstone > 0;
        super.onNeighborBlockUpdate();
        if (poweredRedstone > 0 && !wasPowered) {
            BlockElevatorCaller.setSurroundingElevators(getWorld(), getPos(), thisFloor);
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    void setFloors(ElevatorButton[] floors, int thisFloorLevel) {
        this.floors = floors;
        thisFloor = thisFloorLevel;
        sendDescriptionPacket();
    }

    public ElevatorButton[] getFloors() {
        return floors;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }

    public static class ElevatorButton {
        public final double posX, posY, width, height;
        public final int floorNumber;
        public final int floorHeight;
        public float red, green, blue;
        public String buttonText;

        ElevatorButton(double posX, double posY, double width, double height, int floorNumber, int floorHeight) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.floorNumber = floorNumber;
            this.floorHeight = floorHeight;
            this.buttonText = floorNumber + 1 + "";
        }

        ElevatorButton(CompoundNBT tag) {
            this.posX = tag.getDouble("posX");
            this.posY = tag.getDouble("posY");
            this.width = tag.getDouble("width");
            this.height = tag.getDouble("height");
            this.buttonText = tag.getString("buttonText");
            this.floorNumber = tag.getInt("floorNumber");
            this.floorHeight = tag.getInt("floorHeight");
            this.red = tag.getFloat("red");
            this.green = tag.getFloat("green");
            this.blue = tag.getFloat("blue");
        }

        void setColor(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public CompoundNBT writeToNBT(CompoundNBT tag) {
            tag.putDouble("posX", posX);
            tag.putDouble("posY", posY);
            tag.putDouble("width", width);
            tag.putDouble("height", height);
            tag.putString("buttonText", buttonText);
            tag.putInt("floorNumber", floorNumber);
            tag.putInt("floorHeight", floorHeight);
            tag.putFloat("red", red);
            tag.putFloat("green", green);
            tag.putFloat("blue", blue);
            return tag;
        }
    }
}
