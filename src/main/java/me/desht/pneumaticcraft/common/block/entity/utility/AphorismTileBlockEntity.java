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

package me.desht.pneumaticcraft.common.block.entity.utility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.AphorismTileBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AphorismTileBlockEntity extends AbstractPneumaticCraftBlockEntity {
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

    public void loadSavedData(SavedData savedData) {
        if (savedData != null) {
            // note: not using setter methods here; this happens before level is set, and setters need non-null level
            textLines = savedData.lines.toArray(new String[0]);
            textRotation = savedData.rotation;
            borderColor = savedData.borderColor;
            backgroundColor = savedData.bgColor;
            marginSize = (byte) savedData.margin;
            invisible = savedData.invisible;
            updateLineMetadata();
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        SavedData savedData = componentInput.get(ModDataComponents.APHORISM_TILE_DATA);
        loadSavedData(savedData);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.APHORISM_TILE_DATA, SavedData.forTile(this));
    }

    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeToPacket(tag, provider);

        SavedData.CODEC.encodeStart(NbtOps.INSTANCE, SavedData.forTile(this))
                .ifSuccess(subTag -> tag.put("AphorismData", subTag));
    }

    @Override
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.readFromPacket(tag, provider);

        if (tag.contains("AphorismData")) {
            SavedData.CODEC.parse(NbtOps.INSTANCE, tag.get("AphorismData")).ifSuccess(savedData -> {
                loadSavedData(savedData);
                updateLineMetadata();
                if (level != null) {
                    forceBlockEntityRerender();
                }
            });
        }
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setTextLines(String[] textLines) {
        setTextLines(textLines.length == 0 ? new String[] {""} : textLines, true);
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
        if (level != null) {
            if (level.isClientSide) {
                updateLineMetadata();
            } else {
                // server
                if (notifyClient) sendDescriptionPacket();
            }
        }
        setChanged();
    }

    private void updateLineMetadata() {
        icons = new ItemStack[textLines.length];
        rsLines = new BitSet(textLines.length);
        for (int i = 0; i < textLines.length; i++) {
            Matcher m = ITEM_PAT.matcher(textLines[i]);
            if (m.matches()) {
                icons[i] = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(m.group(1))));
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
        setChanged();
    }

    public byte getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(int marginSize) {
        this.marginSize = (byte) Mth.clamp(marginSize, 0, 9);
        needMaxLineWidthRecalc();
        setChanged();
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
        setChanged();
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

    public record SavedData(List<String>lines, int rotation, int borderColor, int bgColor, int margin, boolean invisible) {
        public static final Codec<SavedData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.listOf().fieldOf("lines").forGetter(SavedData::lines),
                Codec.INT.fieldOf("rotation").forGetter(SavedData::rotation),
                Codec.INT.fieldOf("border").forGetter(SavedData::borderColor),
                Codec.INT.fieldOf("background").forGetter(SavedData::bgColor),
                Codec.INT.fieldOf("margin").forGetter(SavedData::margin),
                Codec.BOOL.fieldOf("invisible").forGetter(SavedData::invisible)
        ).apply(builder, SavedData::new));

        public static final StreamCodec<FriendlyByteBuf, SavedData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SavedData::lines,
                ByteBufCodecs.INT, SavedData::rotation,
                ByteBufCodecs.INT, SavedData::borderColor,
                ByteBufCodecs.INT, SavedData::bgColor,
                ByteBufCodecs.VAR_INT, SavedData::margin,
                ByteBufCodecs.BOOL, SavedData::invisible,
                SavedData::new
        );

        static SavedData forTile(AphorismTileBlockEntity be) {
            return new SavedData(Arrays.asList(be.textLines), be.textRotation,
                    be.borderColor, be.backgroundColor,
                    be.marginSize, be.invisible);
        }
    }
}
