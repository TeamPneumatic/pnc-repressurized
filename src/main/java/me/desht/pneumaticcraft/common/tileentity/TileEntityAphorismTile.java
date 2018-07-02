package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAphorismTile extends TileEntityBase {
    private String[] textLines = new String[]{""};

    public int textRotation;
    private int borderColor = EnumDyeColor.BLUE.getDyeDamage();
    private int backgroundColor = EnumDyeColor.WHITE.getDyeDamage();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        tag.setInteger("lines", textLines.length);
        for (int i = 0; i < textLines.length; i++) {
            tag.setString("line" + i, textLines[i]);
        }
        tag.setInteger("textRot", textRotation);
        tag.setInteger("border", borderColor);
        tag.setInteger("background", backgroundColor);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        int lines = tag.getInteger("lines");
        textLines = new String[lines];
        for (int i = 0; i < lines; i++) {
            textLines[i] = tag.getString("line" + i);
        }
        textRotation = tag.getInteger("textRot");
        borderColor = tag.getInteger("border");
        backgroundColor = tag.getInteger("background");
        if (world != null) rerenderTileEntity();
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setTextLines(String[] textLines) {
        this.textLines = textLines;
        sendDescriptionPacket();
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
        sendDescriptionPacket();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        sendDescriptionPacket();
    }
}
