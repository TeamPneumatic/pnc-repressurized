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

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * A class to manage which sides of a BE's block are mapped to which capability handler objects (item/fluid/energy...)
 */
public class SideConfigurator<T> implements INBTSerializable<CompoundTag> {
    public static final String BASE_BUTTON_TAG = "SideConf";

    // lookup matrix for converting absolute to relative facing based on block's rotation
    private static final RelativeFace[][] FACING_MATRIX = setupFacingMatrix();

    private final List<ConnectionEntry<T>> entries = new ArrayList<>();
    private final String id;
    private final ISideConfigurable sideConfigurable;
    private final Map<String, Integer> idxMap = new HashMap<>();
    private Supplier<T> nullFaceHandler = () -> null;

    // each value here is an index into the 'entries' list
    private final byte[] faces = new byte[RelativeFace.values().length];

    // default face configuration, used to decide if NBT needs to be saved
    private final byte[] defaultFaces = new byte[RelativeFace.values().length];

    /**
     * Constructor
     *
     * @param id a unique string for this configurator's title (I18n: gui.sideConfigurator.title.&lt;titleKey&gt;)
     * @param sideConfigurable the owning object
     */
    public SideConfigurator(String id, ISideConfigurable sideConfigurable) {
        this.id = id;
        this.sideConfigurable = sideConfigurable;
        entries.add(null);  // null represents "unconnected"
    }

    public int registerHandler(String id, ItemStack textureStack, BaseCapability<T,?> cap, Supplier<T> handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, Either.left(textureStack), cap, handler));
        idxMap.put(id, entries.size() - 1);
        return setDefaultSides(defaultRelativeFaces);
    }

    public int registerHandler(String id, ResourceLocation texture, BaseCapability<T,?> cap, Supplier<T> handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, Either.right(texture), cap, handler));
        idxMap.put(id, entries.size() - 1);
        return setDefaultSides(defaultRelativeFaces);
    }

    private int setDefaultSides(RelativeFace... defaultRelativeFaces) {
        Validate.isTrue(entries.size() <= Byte.MAX_VALUE, "No more than " + Byte.MAX_VALUE + " entries allowed");
        byte idx = (byte) (entries.size() - 1);
        for (RelativeFace relativeFace : defaultRelativeFaces) {
            faces[relativeFace.ordinal()] = idx;
            defaultFaces[relativeFace.ordinal()] = idx;
        }
        return idx;
    }

    public void setNullFaceHandler(String id) {
        nullFaceHandler = entries.get(idxMap.get(id)).handler;
    }

    private boolean shouldSaveNBT() {
        return !Arrays.equals(faces, defaultFaces);
    }

    public boolean handleButtonPress(String tag, boolean hasShiftDown) {
        if (tag.startsWith(BASE_BUTTON_TAG)) {
            try {
                RelativeFace relativeFace = RelativeFace.valueOf(tag.split("\\.")[1]);
                cycleValue(relativeFace, hasShiftDown);
                return true;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    public String getButtonTag(RelativeFace relativeFace) {
        return BASE_BUTTON_TAG + "." + relativeFace.toString();
    }

    private void cycleValue(RelativeFace relativeFace, boolean hasShiftDown) {
        int idx = relativeFace.ordinal();
        int n = 0;
        while (n++ < entries.size()) {
            if (hasShiftDown) {
                if (--faces[idx] < 0) {
                    faces[idx] = (byte)(entries.size() - 1);
                }
            } else {
                if (++faces[idx] >= entries.size()) {
                    faces[idx] = 0;
                }
            }
            ConnectionEntry<T> c = entries.get(faces[idx]);
            if (sideConfigurable.isValid(relativeFace, c == null ? null : c.handler.get())) return;
        }
    }

    public String getID() {
        return id;
    }

    public String getTranslationKey() {
        return "pneumaticcraft.gui.sideConfigurator.title." + id;
    }

    public T getHandler(Direction facing) {
        if (facing == null) return nullFaceHandler.get();
        ConnectionEntry<T> c = entries.get(faces[getRelativeFace(facing).ordinal()]);
        return c == null ? null : c.handler.get();
    }

    private static RelativeFace[][] setupFacingMatrix() {
        RelativeFace[][] result = new RelativeFace[4][];
        for (Direction f : DirectionUtil.HORIZONTALS) {
            result[f.get2DDataValue()] = new RelativeFace[4];
            for (RelativeFace rf : RelativeFace.HORIZONTALS) {
                Direction f2 = rot(f, rf);
                result[f.get2DDataValue()][f2.get2DDataValue()] = rf;
            }
        }
        return result;
    }

    private static Direction rot(Direction in, RelativeFace rf) {
        return switch (rf) {
            case RIGHT -> in.getCounterClockWise();
            case LEFT -> in.getClockWise();
            case BACK -> in.getOpposite();
            default -> in;
        };
    }

    private RelativeFace getRelativeFace(Direction facing) {
        return switch (facing) {
            case UP -> RelativeFace.TOP;
            case DOWN -> RelativeFace.BOTTOM;
            default -> FACING_MATRIX[sideConfigurable.byIndex().get2DDataValue()][facing.get2DDataValue()];
        };
    }

    public Component getFaceLabel(RelativeFace relativeFace) {
        ConnectionEntry<T> c = entries.get(faces[relativeFace.ordinal()]);
        return c == null ?
                xlate("pneumaticcraft.gui.sideConfigurator.unconnected") :
                xlate("pneumaticcraft.gui.sideConfigurator." + id + "." + c.id);
    }

    public ConnectionEntry<?> getEntry(RelativeFace face) {
        return entries.get(faces[face.ordinal()]);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag l = new ListTag();
        for (byte face : faces) {
            l.add(ByteTag.valueOf(face));
        }
        tag.put("faces", l);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag l = nbt.getList("faces", Tag.TAG_BYTE);
        for (int i = 0; i < l.size() && i < faces.length; i++) {
            faces[i] = ((ByteTag) l.get(i)).getAsByte();
            if (faces[i] < 0 || faces[i] >= entries.size()) {
                // sanity check: 0th element is always available ("unconnected")
                faces[i] = 0;
            }
        }
    }

    public static CompoundTag writeToNBT(ISideConfigurable sideConfigurable) {
        CompoundTag tag = new CompoundTag();
        for (SideConfigurator<?> sc : sideConfigurable.getSideConfigurators()) {
            if (sc.shouldSaveNBT()) {
                CompoundTag subtag = sc.serializeNBT();
                tag.put(sc.id, subtag);
            }
        }
        return tag;
    }

    public static void readFromNBT(CompoundTag tag, ISideConfigurable sideConfigurable) {
        for (SideConfigurator<?> sc : sideConfigurable.getSideConfigurators()) {
            if (tag.contains(sc.id)) {
                CompoundTag subtag = tag.getCompound(sc.id);
                sc.deserializeNBT(subtag);
            }
        }
    }

    public enum RelativeFace {
        BOTTOM, TOP, LEFT, RIGHT, FRONT, BACK;

        public static final RelativeFace[] HORIZONTALS = new RelativeFace[4];

        static {
            HORIZONTALS[0] = LEFT;
            HORIZONTALS[1] = RIGHT;
            HORIZONTALS[2] = FRONT;
            HORIZONTALS[3] = BACK;
        }
    }

    public record ConnectionEntry<T>(String id, Either<ItemStack,ResourceLocation> texture, BaseCapability<T, ?> cap, Supplier<T> handler) {
    }
}
