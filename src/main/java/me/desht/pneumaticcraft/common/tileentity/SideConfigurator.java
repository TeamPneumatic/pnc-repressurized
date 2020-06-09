package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Predicate;

/**
 * A class to manage which sides of a TE's block are mapped to which capability handler objects (item/fluid/energy...)
 */
public class SideConfigurator<T> implements INBTSerializable<CompoundNBT> {
    private static final String baseButtonTag = "SideConf";

    private final List<ConnectionEntry<T>> entries = new ArrayList<>();
    private final String id;
    private final ISideConfigurable sideConfigurable;
    private final Map<String, Integer> idxMap = new HashMap<>();
    private LazyOptional<T> nullFaceCap = LazyOptional.empty();

    // each value here is an index into the 'entries' list
    private final byte[] faces = new byte[RelativeFace.values().length];
    // default face configuration, used to decide if NBT needs to be saved
    private final byte[] defaultFaces = new byte[RelativeFace.values().length];

    // lookup matrix for converting absolute to relative facing based on block's rotation
    private final RelativeFace[][] facingMatrix = new RelativeFace[4][];

    /**
     * Constructor
     *
     * @param id a unique string for this configurator's title (I18n: gui.sideConfigurator.title.&lt;titleKey&gt;)
     * @param sideConfigurable the owning object
     */
    SideConfigurator(String id, ISideConfigurable sideConfigurable) {
        this.id = id;
        this.sideConfigurable = sideConfigurable;
        entries.add(null);  // null represents "unconnected"

        setupFacingMatrix();
    }

    public int registerHandler(String id, ItemStack textureStack, Capability<T> cap, NonNullSupplier<T> handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, textureStack, cap, handler));
        idxMap.put(id, entries.size() - 1);
        return setDefaultSides(defaultRelativeFaces);
    }

    public int registerHandler(String id, ResourceLocation texture, Capability<T> cap, NonNullSupplier<T> handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, texture, cap, handler));
        idxMap.put(id, entries.size() - 1);
        return setDefaultSides(defaultRelativeFaces);
    }

    public void unregisterHandlers(Predicate<String> idMatcher) {
        List<ConnectionEntry<T>> newEntries = new ArrayList<>();

        for (String id : idxMap.keySet()) {
            if (!idMatcher.test(id)) {
                newEntries.add(entries.get(idxMap.get(id)));
            }
        }

        entries.clear();
        entries.addAll(newEntries);
        idxMap.clear();
        for (int i = 0; i < entries.size(); i++) {
            ConnectionEntry e = entries.get(i);
            idxMap.put(e.id, i);
        }
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

    void setNullFaceHandler(String id) {
        if (nullFaceCap.isPresent()) nullFaceCap.invalidate();
        nullFaceCap = LazyOptional.of(entries.get(idxMap.get(id)).handler);
    }

    private boolean shouldSaveNBT() {
        return !Arrays.equals(faces, defaultFaces);
    }

    void updateHandler(String id, NonNullSupplier<T> handler) {
        int idx = idxMap.get(id);
        ConnectionEntry<T> e = entries.get(idx);
        entries.set(idx, new ConnectionEntry<>(e.id, e.texture, e.cap, handler));
        setNullFaceHandler(id);
    }

    public byte[] getFaces() {
        return faces;
    }

    public void setFaces(byte[] faces) {
        System.arraycopy(faces, 0, this.faces, 0, this.faces.length);
    }

    public boolean handleButtonPress(String tag) {
        try {
            RelativeFace relativeFace = RelativeFace.valueOf(tag.split("\\.")[1]);
            cycleValue(relativeFace);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getButtonId(RelativeFace relativeFace) {
        return baseButtonTag + "." + relativeFace.toString();
    }

    private void cycleValue(RelativeFace relativeFace) {
        int idx = relativeFace.ordinal();
        int n = 0;
        while (n++ < entries.size()) {
            faces[idx]++;
            if (faces[idx] >= entries.size()) {
                faces[idx] = 0;
            }
            ConnectionEntry<T> c = entries.get(faces[idx]);
            if (sideConfigurable.isValid(relativeFace, c == null ? null : c.cap)) return;
        }
    }

    public String getID() {
        return id;
    }

    public String getTranslationKey() {
        return "pneumaticcraft.gui.sideConfigurator.title." + id;
    }


    LazyOptional<T> getHandler(Direction facing) {
        if (facing == null) return nullFaceCap;
        ConnectionEntry<T> c = entries.get(faces[getRelativeFace(facing).ordinal()]);
        return c == null ? LazyOptional.empty() : c.lazy;
    }

    void setupFacingMatrix() {
        for (Direction f : PneumaticCraftUtils.HORIZONTALS) {
            facingMatrix[f.getHorizontalIndex()] = new RelativeFace[4];
            for (RelativeFace rf : RelativeFace.HORIZONTALS) {
                Direction f2 = rot(f, rf);
                facingMatrix[f.getHorizontalIndex()][f2.getHorizontalIndex()] = rf;
            }
        }
    }

    private Direction rot(Direction in, RelativeFace rf) {
        switch (rf) {
            case RIGHT: return in.rotateYCCW();
            case LEFT: return in.rotateY();
            case BACK: return in.getOpposite();
            default: return in;
        }
    }

    private RelativeFace getRelativeFace(Direction facing) {
        if (facing == Direction.UP) {
            return RelativeFace.TOP;
        } else if (facing == Direction.DOWN) {
            return RelativeFace.BOTTOM;
        } else {
            return facingMatrix[sideConfigurable.byIndex().getHorizontalIndex()][facing.getHorizontalIndex()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void setupButton(WidgetButtonExtended button) {
        try {
            RelativeFace relativeFace = RelativeFace.valueOf(button.getTag().split("\\.")[1]);
            ConnectionEntry c = entries.get(faces[relativeFace.ordinal()]);
            if (c != null) {
                if (c.texture instanceof ItemStack) {
                    button.setRenderStacks((ItemStack) c.texture);
                    button.setRenderedIcon(null);
                } else if (c.texture instanceof ResourceLocation) {
                    button.setRenderStacks(ItemStack.EMPTY);
                    button.setRenderedIcon((ResourceLocation) c.texture);
                }
            } else {
                button.setRenderStacks(ItemStack.EMPTY);
                button.setRenderedIcon(Textures.GUI_X_BUTTON);
            }
            button.setTooltipText(ImmutableList.of(TextFormatting.YELLOW + relativeFace.toString(), I18n.format(getFaceKey(relativeFace))));
        } catch (IllegalArgumentException e) {
            Log.warning("Bad tag '" + button.getTag() + "'");
        }
    }

    private String getFaceKey(RelativeFace relativeFace) {
        ConnectionEntry<T> c = entries.get(faces[relativeFace.ordinal()]);
        return c == null ? "pneumaticcraft.gui.sideConfigurator.unconnected" : "pneumaticcraft.gui.sideConfigurator." + id + "." + c.id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT l = new ListNBT();
        for (byte face : faces) {
            l.add(ByteNBT.valueOf(face));
        }
        tag.put("faces", l);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT l = nbt.getList("faces", Constants.NBT.TAG_BYTE);
        for (int i = 0; i < l.size() && i < faces.length; i++) {
            faces[i] = ((ByteNBT) l.get(i)).getByte();
        }
    }

    public static CompoundNBT writeToNBT(ISideConfigurable sideConfigurable) {
        CompoundNBT tag = new CompoundNBT();
        for (SideConfigurator<?> sc : sideConfigurable.getSideConfigurators()) {
            if (sc.shouldSaveNBT()) {
                CompoundNBT subtag = sc.serializeNBT();
                tag.put(sc.id, subtag);
            }
        }
        return tag;
    }

    public static void readFromNBT(CompoundNBT tag, ISideConfigurable sideConfigurable) {
        for (SideConfigurator<?> sc : sideConfigurable.getSideConfigurators()) {
            if (tag.contains(sc.id)) {
                CompoundNBT subtag = tag.getCompound(sc.id);
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

    private static class ConnectionEntry<T> {
        private final String id;
        private final Object texture;
        private final Capability<T> cap;
        private final NonNullSupplier<T> handler;
        private final LazyOptional<T> lazy;

        private ConnectionEntry(String id, Object texture, Capability<T> cap, NonNullSupplier<T> handler) {
            this.id = id;
            this.texture = texture;
            this.cap = cap;
            this.handler = handler;
            this.lazy = LazyOptional.of(handler);
        }
    }
}
