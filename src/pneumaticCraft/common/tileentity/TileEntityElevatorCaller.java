package pneumaticCraft.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import pneumaticCraft.common.network.DescSynced;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityElevatorCaller extends TileEntityBase{
    private ElevatorButton[] floors = new ElevatorButton[0];
    public int thisFloor;
    private boolean emittingRedstone;
    private boolean shouldUpdateNeighbors;
    @DescSynced
    public ItemStack camoStack;
    public Block camoBlock;

    public void setEmittingRedstone(boolean emittingRedstone){
        if(emittingRedstone != this.emittingRedstone) {
            this.emittingRedstone = emittingRedstone;
            shouldUpdateNeighbors = true;
        }
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(shouldUpdateNeighbors) {
            updateNeighbours();
            shouldUpdateNeighbors = false;
        }
    }

    public boolean getEmittingRedstone(){
        return emittingRedstone;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        emittingRedstone = tag.getBoolean("emittingRedstone");
        thisFloor = tag.getInteger("thisFloor");
        camoStack = tag.hasKey("camoStack") ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("camoStack")) : null;
        shouldUpdateNeighbors = tag.getBoolean("shouldUpdateNeighbors");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("emittingRedstone", emittingRedstone);
        tag.setInteger("thisFloor", thisFloor);
        if(camoStack != null) {
            NBTTagCompound camoTag = new NBTTagCompound();
            camoStack.writeToNBT(camoTag);
            tag.setTag("camoStack", camoTag);
        }
        tag.setBoolean("shouldUpdateNeighbors", shouldUpdateNeighbors);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        int floorAmount = tag.getInteger("floors");
        floors = new ElevatorButton[floorAmount];
        for(int i = 0; i < floorAmount; i++) {
            NBTTagCompound buttonTag = tag.getCompoundTag("floor" + i);
            floors[i] = new ElevatorButton();
            floors[i].readFromNBT(buttonTag);
        }
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        tag.setInteger("floors", floors.length);
        for(ElevatorButton floor : floors) {
            NBTTagCompound buttonTag = new NBTTagCompound();
            floor.writeToNBT(buttonTag);
            tag.setTag("floor" + floor.floorNumber, buttonTag);
        }
    }

    @Override
    public void onDescUpdate(){
        Block newCamo = camoStack != null && camoStack.getItem() instanceof ItemBlock ? ((ItemBlock)camoStack.getItem()).field_150939_a : null;

        if(newCamo != camoBlock) {
            camoBlock = newCamo;
            rerenderChunk();
        }
    }

    public void setFloors(ElevatorButton[] floors, int thisFloorLevel){
        this.floors = floors;
        thisFloor = thisFloorLevel;
        sendDescriptionPacket();
    }

    public ElevatorButton[] getFloors(){
        return floors;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    public static class ElevatorButton{
        public double posX, posY, width, height;
        public float red, green, blue;
        public String buttonText = "";
        public int floorNumber;
        public int floorHeight;

        public ElevatorButton(double posX, double posY, double width, double height, int floorNumber, int floorHeight){
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.floorNumber = floorNumber;
            this.floorHeight = floorHeight;
            buttonText = floorNumber + 1 + "";
        }

        public ElevatorButton(){}

        public void setColor(float red, float green, float blue){
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public void writeToNBT(NBTTagCompound tag){
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

        public void readFromNBT(NBTTagCompound tag){
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
