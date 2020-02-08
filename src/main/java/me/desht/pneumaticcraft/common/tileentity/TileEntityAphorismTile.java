package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TileEntityAphorismTile extends TileEntityBase {
    public static final String NBT_BORDER_COLOR = "borderColor";
    public static final String NBT_BACKGROUND_COLOR = "backgroundColor";
    private static final String NBT_TEXT_ROTATION = "textRot";
    public static final String NBT_TEXT_LINES = "lines";

    private String[] textLines = new String[]{""};

    public int textRotation;
    private int borderColor = DyeColor.BLUE.getId();
    private int backgroundColor = DyeColor.WHITE.getId();
    private int maxLineWidth = -1;  // cached width for rendering purposes

    public TileEntityAphorismTile() {
        super(ModTileEntities.APHORISM_TILE.get());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        return true;
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag) {
        writeToPacket(blockEntityTag);
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        CompoundNBT subTag = new CompoundNBT();
        subTag.put(NBT_TEXT_LINES, Arrays.stream(textLines).map(StringNBT::new).collect(Collectors.toCollection(ListNBT::new)));
        subTag.putInt(NBT_TEXT_ROTATION, textRotation);
        subTag.putInt(NBT_BORDER_COLOR, borderColor);
        subTag.putInt(NBT_BACKGROUND_COLOR, backgroundColor);
        tag.put(NBTKeys.NBT_EXTRA, subTag);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        if (tag.contains(NBTKeys.NBT_EXTRA)) {
            CompoundNBT subTag = tag.getCompound(NBTKeys.NBT_EXTRA);
            ListNBT l = subTag.getList(NBT_TEXT_LINES, Constants.NBT.TAG_STRING);
            if (l.isEmpty()) {
                textLines = new String[] { "" };
            } else {
                textLines = new String[l.size()];
                IntStream.range(0, textLines.length).forEach(i -> textLines[i] = l.getString(i));
            }
            textRotation = subTag.getInt(NBT_TEXT_ROTATION);
            if (subTag.contains(NBT_BORDER_COLOR)) {
                borderColor = subTag.getInt(NBT_BORDER_COLOR);
                backgroundColor = subTag.getInt(NBT_BACKGROUND_COLOR);
            } else {
                borderColor = DyeColor.BLUE.getId();
                backgroundColor = DyeColor.WHITE.getId();
            }
            if (world != null) rerenderTileEntity();
        }
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setTextLines(String[] textLines) {
        setTextLines(textLines, true);
    }

    public void setTextLines(String[] textLines, boolean notifyClient) {
        this.textLines = textLines;
        this.maxLineWidth = -1; // force recalc
        if (!world.isRemote && notifyClient) sendDescriptionPacket();
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

    // client only!
    public int getMaxLineWidth() {
        if (maxLineWidth < 0) {
            for (String line : textLines) {
                int stringWidth = ClientUtils.getStringWidth(line);
                if (stringWidth > maxLineWidth) {
                    maxLineWidth = stringWidth;
                }
            }
            maxLineWidth *= 1.05;  // multiplier allows for a small margin; looks better
        }
        return maxLineWidth;
    }
}
