/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.AphorismTileBlock;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AphorismTileBlockEntity extends AbstractPneumaticCraftBlockEntity {
    public static final String NBT_BORDER_COLOR = "borderColor";
    public static final String NBT_BACKGROUND_COLOR = "backgroundColor";
    private static final String NBT_TEXT_ROTATION = "textRot";
    public static final String NBT_TEXT_LINES = "lines";
    public static final String NBT_MARGIN = "margin";
    private static final String NBT_INVISIBLE = "invisible";

    private static final Pattern ITEM_PAT = Pattern.compile("^\\{item:(\\w+:[a-z0-9_.]+)}$");

    private String[] textLines = new String[]{""};
    private ItemStack[] icons = new ItemStack[]{ItemStack.EMPTY};
    private BitSet rsLines = new BitSet(1);

    private int textRotation;
    private int borderColor = DyeColor.BLUE.getId();
    private int backgroundColor = DyeColor.WHITE.getId();
    private int maxLineWidth = -1;  // cached width for rendering purposes
    private byte marginSize; // 0..9
    private boolean invisible;
    public int currentRedstonePower = 0;
    private long lastPoll = 0L;
    public int cursorX = -1, cursorY = -1; // stored in client BE only to remember last editor cursor pos

    public AphorismTileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.APHORISM_TILE.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        return true;
    }

    @Override
    public void serializeExtraItemData(CompoundTag blockEntityTag, boolean preserveState) {
        writeToPacket(blockEntityTag);
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        CompoundTag subTag = new CompoundTag();
        subTag.put(NBT_TEXT_LINES, Arrays.stream(textLines).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new)));
        subTag.putInt(NBT_TEXT_ROTATION, textRotation);
        subTag.putInt(NBT_BORDER_COLOR, borderColor);
        subTag.putInt(NBT_BACKGROUND_COLOR, backgroundColor);
        subTag.putByte(NBT_MARGIN, marginSize);
        subTag.putBoolean(NBT_INVISIBLE, invisible);
        tag.put(NBTKeys.NBT_EXTRA, subTag);
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        if (tag.contains(NBTKeys.NBT_EXTRA)) {
            CompoundTag subTag = tag.getCompound(NBTKeys.NBT_EXTRA);
            ListTag l = subTag.getList(NBT_TEXT_LINES, Tag.TAG_STRING);
            if (l.isEmpty()) {
                textLines = new String[] { "" };
            } else {
                textLines = new String[l.size()];
                IntStream.range(0, textLines.length).forEach(i -> textLines[i] = l.getString(i));
            }
            updateLineMetadata();
            textRotation = subTag.getInt(NBT_TEXT_ROTATION);
            if (subTag.contains(NBT_BORDER_COLOR)) {
                borderColor = subTag.getInt(NBT_BORDER_COLOR);
                backgroundColor = subTag.getInt(NBT_BACKGROUND_COLOR);
            } else {
                borderColor = DyeColor.BLUE.getId();
                backgroundColor = DyeColor.WHITE.getId();
            }
            setMarginSize(subTag.getByte(NBT_MARGIN));
            setInvisible(subTag.getBoolean(NBT_INVISIBLE));
            if (level != null) rerenderTileEntity();
        }
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setTextLines(String[] textLines) {
        setTextLines(textLines, true);
    }

    public int getTextRotation() {
        return textRotation;
    }

    public void setTextRotation(int textRotation) {
        this.textRotation = textRotation;
    }

    public void setTextLines(String[] textLines, boolean notifyClient) {
        this.textLines = textLines;
        this.maxLineWidth = -1; // force recalc
        icons = new ItemStack[textLines.length];
        if (nonNullLevel().isClientSide) {
            updateLineMetadata();
        } else {
            // server
            if (notifyClient) sendDescriptionPacket();
        }
    }

    private void updateLineMetadata() {
        icons = new ItemStack[textLines.length];
        rsLines = new BitSet(textLines.length);
        for (int i = 0; i < textLines.length; i++) {
            Matcher m = ITEM_PAT.matcher(textLines[i]);
            if (m.matches()) {
                icons[i] = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(m.group(1))));
            } else {
                icons[i] = ItemStack.EMPTY;
                if (textLines[i].contains("{redstone}")) {
                    rsLines.set(i);
                }
            }
        }
    }

    public ItemStack getIconAt(int line) {
        return line >= 0 && line < icons.length ? icons[line] : ItemStack.EMPTY;
    }

    public boolean isRedstoneLine(int line) {
        return (line >= 0 && line < rsLines.size()) && rsLines.get(line);
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
        if (!nonNullLevel().isClientSide) sendDescriptionPacket();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        if (!nonNullLevel().isClientSide) sendDescriptionPacket();
    }

    public byte getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(int marginSize) {
        this.marginSize = (byte) Mth.clamp(marginSize, 0, 9);
        needMaxLineWidthRecalc();
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getBlock() instanceof AphorismTileBlock) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AphorismTileBlock.INVISIBLE, invisible));
            }
        }
    }

    public int getMaxLineWidth(boolean editing) {
        // client only!
        if (maxLineWidth < 0) {
            for (int i = 0; i < textLines.length; i++) {
                String line = textLines[i];
                if (!editing && isRedstoneLine(i)) line = line.replaceAll(Pattern.quote("{redstone}"), Integer.toString(currentRedstonePower));
                int stringWidth = !editing && !getIconAt(i).isEmpty() ? 6 : ClientUtils.getStringWidth(line);
                if (stringWidth > maxLineWidth) {
                    maxLineWidth = stringWidth;
                }
            }
            float mul = 1f + (marginSize + 1) * 0.075f;
            maxLineWidth *= mul;  // multiplier allows for a small margin; looks better
        }
        return maxLineWidth;
    }

    public void needMaxLineWidthRecalc() {
        maxLineWidth = -1;
    }

    public int pollRedstone() {
        Level level = nonNullLevel();
        if (level.getGameTime() - lastPoll >= 2) {
            Direction d = getRotation();
            int p = nonNullLevel().getSignal(worldPosition.relative(d), d);
            if (p != currentRedstonePower) needMaxLineWidthRecalc();
            currentRedstonePower = p;
            lastPoll = nonNullLevel().getGameTime();
        }
        return currentRedstonePower;
    }

    public Pair<Integer,Integer> getCursorPos() {
        int cx, cy;
        cy = cursorY >= 0 && cursorY < textLines.length ? cursorY : textLines.length - 1;
        cx = cursorX >= 0 && cursorX <= textLines[cy].length() ? cursorX : textLines[cy].length();
        return Pair.of(cx, cy);
    }

    public void setCursorPos(int cursorX, int cursorY) {
        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }
}
