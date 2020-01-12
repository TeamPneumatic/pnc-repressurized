package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityAphorismTile extends TileEntityBase {
    private String[] textLines = new String[]{""};

    public int textRotation;
    private int borderColor = DyeColor.BLUE.getId();
    private int backgroundColor = DyeColor.WHITE.getId();

    public TileEntityAphorismTile() {
        super(ModTileEntities.APHORISM_TILE.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        tag.putInt("lines", textLines.length);
        for (int i = 0; i < textLines.length; i++) {
            tag.putString("line" + i, textLines[i]);
        }
        tag.putInt("textRot", textRotation);
        tag.putInt("border", borderColor);
        tag.putInt("background", backgroundColor);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        int lines = tag.getInt("lines");
        textLines = new String[lines];
        for (int i = 0; i < lines; i++) {
            textLines[i] = tag.getString("line" + i);
        }
        textRotation = tag.getInt("textRot");
        if (tag.contains("border")) {
            borderColor = tag.getInt("border");
            backgroundColor = tag.getInt("background");
        } else {
            borderColor = DyeColor.BLUE.getId();
            backgroundColor = DyeColor.WHITE.getId();
        }
        if (world != null) rerenderTileEntity();
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setTextLines(String[] textLines) {
        this.textLines = textLines;
        if (!world.isRemote) sendDescriptionPacket();
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
        if (!world.isRemote) sendDescriptionPacket();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        if (!world.isRemote) sendDescriptionPacket();
    }
}
