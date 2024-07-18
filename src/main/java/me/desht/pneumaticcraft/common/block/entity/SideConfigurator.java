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

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * A class to manage which sides of a BE's block are mapped to which capability handler objects (item/fluid/energy...)
 */
public class SideConfigurator<T> {
    public static final String BASE_BUTTON_TAG = "SideConf";

    private final List<ConnectionEntry<T>> entries = new ArrayList<>();
    private final String id;
    private final ISideConfigurable sideConfigurable;
    private final Map<String, Integer> idxMap = new HashMap<>();
    private Supplier<T> nullFaceHandler = () -> null;

    // each value here is an index into the 'entries' list
    private final Map<RelativeFace,Integer> faces = new EnumMap<>(RelativeFace.class);

    // default face configuration, used to decide if NBT needs to be saved
    private final Map<RelativeFace,Integer> defaultFaces = new EnumMap<>(RelativeFace.class);

    // lookup matrix for converting absolute to relative facing based on block's rotation
    private final RelativeFace[][] facingMatrix = new RelativeFace[4][];

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

        setupFacingMatrix();
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
        int idx = entries.size() - 1;
        for (RelativeFace relativeFace : defaultRelativeFaces) {
            faces.put(relativeFace, idx);
            defaultFaces.put(relativeFace, idx);
        }
        return idx;
    }

    public void setNullFaceHandler(String id) {
        nullFaceHandler = entries.get(idxMap.get(id)).handler;
    }

    private boolean shouldSaveNBT() {
        return !faces.equals(defaultFaces);
    }

    public void updateHandler(String id, Supplier<T> handler) {
        int idx = idxMap.get(id);
        ConnectionEntry<T> e = entries.get(idx);
        entries.set(idx, new ConnectionEntry<>(e.id, e.texture, e.cap, handler));
        setNullFaceHandler(id);
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
        int n = 0;
        while (n++ < entries.size()) {
            cycleFace(relativeFace, hasShiftDown ? -1 : 1);
            ConnectionEntry<T> c = entries.get(getFaceIndex(relativeFace));
            if (sideConfigurable.isValid(relativeFace, c == null ? null : c.handler.get())) return;
        }
    }

    private void cycleFace(RelativeFace idx, int dir) {
        int newVal = getFaceIndex(idx) + dir;
        if (newVal < 0) {
            newVal = entries.size() - 1;
        } else if (newVal >= entries.size()) {
            newVal = 0;
        }
        faces.put(idx, newVal);
    }

    public String getID() {
        return id;
    }

    public String getTranslationKey() {
        return "pneumaticcraft.gui.sideConfigurator.title." + id;
    }

    public T getHandler(Direction facing) {
        if (facing == null) return nullFaceHandler.get();
        ConnectionEntry<T> c = entries.get(getFaceIndex(getRelativeFace(facing)));
        return c == null ? null : c.handler.get();
    }

    void setupFacingMatrix() {
        for (Direction f : DirectionUtil.HORIZONTALS) {
            facingMatrix[f.get2DDataValue()] = new RelativeFace[4];
            for (RelativeFace rf : RelativeFace.HORIZONTALS) {
                Direction f2 = rot(f, rf);
                facingMatrix[f.get2DDataValue()][f2.get2DDataValue()] = rf;
            }
        }
    }

    private Direction rot(Direction in, RelativeFace rf) {
        return switch (rf) {
            case RIGHT -> in.getCounterClockWise();
            case LEFT -> in.getClockWise();
            case BACK -> in.getOpposite();
            default -> in;
        };
    }

    private RelativeFace getRelativeFace(Direction facing) {
        if (facing == Direction.UP) {
            return RelativeFace.TOP;
        } else if (facing == Direction.DOWN) {
            return RelativeFace.BOTTOM;
        } else {
            return facingMatrix[sideConfigurable.byIndex().get2DDataValue()][facing.get2DDataValue()];
        }
    }

    public Component getFaceLabel(RelativeFace relativeFace) {
        ConnectionEntry<T> c = entries.get(getFaceIndex(relativeFace));
        return c == null ?
                xlate("pneumaticcraft.gui.sideConfigurator.unconnected") :
                xlate("pneumaticcraft.gui.sideConfigurator." + id + "." + c.id);
    }

    public ConnectionEntry<?> getEntry(RelativeFace relativeFace) {
        return entries.get(getFaceIndex(relativeFace));
    }

    private int getFaceIndex(RelativeFace relativeFace) {
        return faces.getOrDefault(relativeFace, 0);
    }

    public static Tag writeToNBT(ISideConfigurable sideConfigurable, HolderLookup.Provider provider) {
        Map<String,Saved> saveMap = buildSavedMap(sideConfigurable);
        return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), saveMap)
                .resultOrPartial(err -> Log.error("can't encode side config: {}", err))
                .orElse(new CompoundTag());
    }

    public static void readFromNBT(CompoundTag tag, ISideConfigurable sideConfigurable, HolderLookup.Provider provider) {
        CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(err -> Log.error("can't decode side config: {}", err))
                .ifPresent(data -> loadSavedData(sideConfigurable, data));
    }

    public static @NotNull Map<String, Saved> buildSavedMap(ISideConfigurable sideConfigurable) {
        return sideConfigurable.getSideConfigurators().stream()
                .filter(SideConfigurator::shouldSaveNBT)
                .collect(Collectors.toMap(sc -> sc.id, Saved::fromConfigurator, (a, b) -> b));
    }

    public static void loadSavedData(ISideConfigurable sideConfigurable, Map<String, Saved> saveMap) {
        sideConfigurable.getSideConfigurators().stream()
                .filter(sc -> saveMap.containsKey(sc.id))
                .forEach(sc -> saveMap.get(sc.id).loadInto(sc));
    }

    public static final Codec<Map<String,Saved>> CODEC = Codec.unboundedMap(Codec.STRING, Saved.CODEC);
    public static final StreamCodec<FriendlyByteBuf,Map<String,Saved>> STREAM_CODEC
        = ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, Saved.STREAM_CODEC);

    public enum RelativeFace implements StringRepresentable {
        BOTTOM("bottom"),
        TOP("top"),
        LEFT("left"),
        RIGHT("right"),
        FRONT("front"),
        BACK("back");

        public static final RelativeFace[] HORIZONTALS = new RelativeFace[4];

        public static final Codec<RelativeFace> CODEC = StringRepresentable.fromEnum(RelativeFace::values);

        static {
            HORIZONTALS[0] = LEFT;
            HORIZONTALS[1] = RIGHT;
            HORIZONTALS[2] = FRONT;
            HORIZONTALS[3] = BACK;
        }
        private final String name;


        RelativeFace(String name) {
            this.name = name;
        }
        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record ConnectionEntry<T>(String id, Either<ItemStack,ResourceLocation> texture, BaseCapability<T, ?> cap, Supplier<T> handler) {
    }

    public record Saved(Map<RelativeFace,Integer> faces) {
        // right now, only the face mapping data, but using a record for future flexibility
        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.unboundedMap(RelativeFace.CODEC, Codec.INT).fieldOf("faces").forGetter(Saved::faces)
        ).apply(builder, Saved::new));
        public static final StreamCodec<FriendlyByteBuf, Saved> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, NeoForgeStreamCodecs.enumCodec(RelativeFace.class), ByteBufCodecs.VAR_INT), Saved::faces,
                Saved::new
        );

        public static Saved fromConfigurator(SideConfigurator<?> configurator) {
            return new Saved(configurator.faces);
        }

        public void loadInto(SideConfigurator<?> configurator) {
            faces.forEach((face, idx) ->
                    configurator.faces.put(face, Mth.clamp(idx, 0, configurator.entries.size() - 1))
            );
        }
    }

}
