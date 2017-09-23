package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockElevatorCaller;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class TileEntityElevatorCaller extends TileEntityBase {
    private ElevatorButton[] floors = new ElevatorButton[0];
    private int thisFloor;
    private boolean emittingRedstone;
    private boolean shouldUpdateNeighbors;
    @DescSynced
    public @Nonnull ItemStack camoStack = ItemStack.EMPTY;
    private Block camoBlock;

    public void setEmittingRedstone(boolean emittingRedstone) {
        if (emittingRedstone != this.emittingRedstone) {
            this.emittingRedstone = emittingRedstone;
            shouldUpdateNeighbors = true;
        }
    }

    @Override
    public void update() {
        super.update();
        if (shouldUpdateNeighbors) {
            updateNeighbours();
            shouldUpdateNeighbors = false;
        }
    }

    public boolean getEmittingRedstone() {
        return emittingRedstone;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        emittingRedstone = tag.getBoolean("emittingRedstone");
        thisFloor = tag.getInteger("thisFloor");
        camoStack = tag.hasKey("camoStack") ? new ItemStack(tag.getCompoundTag("camoStack")) : ItemStack.EMPTY;
        shouldUpdateNeighbors = tag.getBoolean("shouldUpdateNeighbors");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("emittingRedstone", emittingRedstone);
        tag.setInteger("thisFloor", thisFloor);
        if (camoStack != ItemStack.EMPTY) {
            NBTTagCompound camoTag = new NBTTagCompound();
            camoStack.writeToNBT(camoTag);
            tag.setTag("camoStack", camoTag);
        }
        tag.setBoolean("shouldUpdateNeighbors", shouldUpdateNeighbors);
        return tag;
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        int floorAmount = tag.getInteger("floors");
        floors = new ElevatorButton[floorAmount];
        for (int i = 0; i < floorAmount; i++) {
            NBTTagCompound buttonTag = tag.getCompoundTag("floor" + i);
            floors[i] = new ElevatorButton();
            floors[i].readFromNBT(buttonTag);
        }
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        tag.setInteger("floors", floors.length);
        for (ElevatorButton floor : floors) {
            NBTTagCompound buttonTag = new NBTTagCompound();
            floor.writeToNBT(buttonTag);
            tag.setTag("floor" + floor.floorNumber, buttonTag);
        }
    }

    @Override
    public void onDescUpdate() {
        Block newCamo = camoStack.getItem() instanceof ItemBlock ? ((ItemBlock) camoStack.getItem()).getBlock() : null;

        if (newCamo != camoBlock) {
            camoBlock = newCamo;
            rerenderChunk();
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

    void setFloors(ElevatorButton[] floors, int thisFloorLevel) {
        this.floors = floors;
        thisFloor = thisFloorLevel;
        sendDescriptionPacket();
    }

    public ElevatorButton[] getFloors() {
        return floors;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    public static class ElevatorButton {
        public double posX, posY, width, height;
        public float red, green, blue;
        public String buttonText = "";
        public int floorNumber;
        public int floorHeight;

        public ElevatorButton(double posX, double posY, double width, double height, int floorNumber, int floorHeight) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.floorNumber = floorNumber;
            this.floorHeight = floorHeight;
            buttonText = floorNumber + 1 + "";
        }

        public ElevatorButton() {
        }

        public void setColor(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public void writeToNBT(NBTTagCompound tag) {
            tag.setDouble("posX", posX);
            tag.setDouble("posY", posY);
            tag.setDouble("width", width);
            tag.setDouble("height", height);
            tag.setString("buttonText", buttonText);
            tag.setInteger("floorNumber", floorNumber);
            tag.setInteger("floorHeight", floorHeight);
            tag.setFloat("red", red);
            tag.setFloat("green", green);
            tag.setFloat("blue", blue);
        }

        public void readFromNBT(NBTTagCompound tag) {
            posX = tag.getDouble("posX");
            posY = tag.getDouble("posY");
            width = tag.getDouble("width");
            height = tag.getDouble("height");
            buttonText = tag.getString("buttonText");
            floorNumber = tag.getInteger("floorNumber");
            floorHeight = tag.getInteger("floorHeight");
            red = tag.getFloat("red");
            green = tag.getFloat("green");
            blue = tag.getFloat("blue");
        }

        /*
        @Override
        public boolean equals(Object button){
            if(!(button instanceof ElevatorButton)) return false;
            ElevatorButton b = (ElevatorButton)button;
            return posX == b.posX && posY == b.posY && width == b.width && height == b.height && buttonText.equals(b.buttonText) && floorNumber == b.floorNumber && floorHeight == b.floorHeight && red == b.red && green == ;
        }

        @Override
        public int hashCode(){
            return (int)(posX * 191 + posY * 281 + width * 342 + height * 425) + buttonText.hashCode() + floorNumber * 1041 + floorHeight * 1024;
        }*/

    }
}
