package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import java.util.*;

/**
 * A class to manage which sides of a TE's block are mapped to which capability handler objects (item/fluid/energy...)
 */
public class SideConfigurator<T> implements INBTSerializable<NBTTagCompound> {
    private final List<ConnectionEntry<T>> entries = new ArrayList<>();
    private final String id;
    private final ISideConfigurable sideConfigurable;
    private final int baseButtonID;
    private final Map<String, Integer> idxMap = new HashMap<>();
    private T nullFaceHandler = null;

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
     * @param baseButtonID id of the first GUI button; there are six in total, one for each side of the block
     */
    SideConfigurator(String id, ISideConfigurable sideConfigurable, int baseButtonID) {
        this.id = id;
        this.sideConfigurable = sideConfigurable;
        this.baseButtonID = baseButtonID;
        entries.add(null);  // null represents "unconnected"

        setupFacingMatrix();
    }

    int registerHandler(String id, ItemStack textureStack, Capability<T> cap, T handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, textureStack, cap, handler));
        idxMap.put(id, entries.size() - 1);
        return setDefaultSides(defaultRelativeFaces);
    }

    int registerHandler(String id, ResourceLocation texture, Capability<T> cap, T handler, RelativeFace... defaultRelativeFaces) {
        entries.add(new ConnectionEntry<>(id, texture, cap, handler));
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

    void setNullFaceHandler(String id) {
        nullFaceHandler = entries.get(idxMap.get(id)).handler;
    }

    private boolean shouldSaveNBT() {
        return !Arrays.equals(faces, defaultFaces);
    }

    void updateHandler(String id, T handler) {
        int idx = idxMap.get(id);
        ConnectionEntry e = entries.get(idx);
        entries.set(idx, new ConnectionEntry<T>(e.id, e.texture, e.cap, handler));
        setNullFaceHandler(id);
    }

    public int getBaseButtonID() {
        return baseButtonID;
    }

    public byte[] getFaces() {
        return faces;
    }

    public void setFaces(byte[] faces) {
        System.arraycopy(faces, 0, this.faces, 0, this.faces.length);
    }

    public boolean handleButtonPress(int buttonID) {
        if (buttonID >= baseButtonID && buttonID < baseButtonID + 6) {
            RelativeFace relativeFace = RelativeFace.values()[buttonID - baseButtonID];
            cycleValue(relativeFace);
            return true;
        }
        return false;
    }

    public int getButtonId(RelativeFace relativeFace) {
        return baseButtonID + relativeFace.ordinal();
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
        return "gui.sideConfigurator.title." + id;
    }


    T getHandler(EnumFacing facing) {
        if (facing == null) return nullFaceHandler;
        ConnectionEntry<T> c = entries.get(faces[getRelativeFace(facing).ordinal()]);
        return c == null ? null : c.handler;
    }

    void setupFacingMatrix() {
        for (EnumFacing f : EnumFacing.HORIZONTALS) {
            facingMatrix[f.getHorizontalIndex()] = new RelativeFace[4];
            for (RelativeFace rf : RelativeFace.HORIZONTALS) {
                EnumFacing f2 = rot(f, rf);
                facingMatrix[f.getHorizontalIndex()][f2.getHorizontalIndex()] = rf;
            }
        }
    }

    private EnumFacing rot(EnumFacing in, RelativeFace rf) {
        switch (rf) {
            case RIGHT: return in.rotateYCCW();
            case LEFT: return in.rotateY();
            case BACK: return in.getOpposite();
            default: return in;
        }
    }

    private RelativeFace getRelativeFace(EnumFacing facing) {
        if (facing == EnumFacing.UP) {
            return RelativeFace.TOP;
        } else if (facing == EnumFacing.DOWN) {
            return RelativeFace.BOTTOM;
        } else {
            return facingMatrix[sideConfigurable.getFront().getHorizontalIndex()][facing.getHorizontalIndex()];
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupButton(GuiButtonSpecial button) {
        RelativeFace relativeFace = RelativeFace.values()[button.getID() - baseButtonID];
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
        button.setTooltipText(Lists.newArrayList(TextFormatting.YELLOW + relativeFace.toString(), I18n.format(getFaceKey(relativeFace))));
    }

    private String getFaceKey(RelativeFace relativeFace) {
        ConnectionEntry<T> c = entries.get(faces[relativeFace.ordinal()]);
        return c == null ? "gui.sideConfigurator.unconnected" : "gui.sideConfigurator." + id + "." + c.id;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList l = new NBTTagList();
        for (byte face : faces) {
            l.appendTag(new NBTTagByte(face));
        }
        tag.setTag("faces", l);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList l = nbt.getTagList("faces", Constants.NBT.TAG_BYTE);
        for (int i = 0; i < l.tagCount() && i < faces.length; i++) {
            faces[i] = ((NBTTagByte) l.get(i)).getByte();
        }
    }

    public static NBTTagCompound writeToNBT(ISideConfigurable sideConfigurable) {
        NBTTagCompound tag = new NBTTagCompound();
        for (SideConfigurator sc : sideConfigurable.getSideConfigurators()) {
            if (sc.shouldSaveNBT()) {
                NBTTagCompound subtag = sc.serializeNBT();
                tag.setTag(sc.id, subtag);
            }
        }
        return tag;
    }

    public static void readFromNBT(NBTTagCompound tag, ISideConfigurable sideConfigurable) {
        for (SideConfigurator sc : sideConfigurable.getSideConfigurators()) {
            if (tag.hasKey(sc.id)) {
                NBTTagCompound subtag = tag.getCompoundTag(sc.id);
                sc.deserializeNBT(subtag);
            }
        }
    }

    /**
     * Side configurable blocks must be rotatable.  This helper method catches any horizontally-rotatable blocks from
     * older worlds which didn't have the ROTATION property and got a potentially invalid rotation by default
     * (probably DOWN).
     *
     * @param te the tile entity whose block needs checking
     */
    static void validateBlockRotation(TileEntityBase te) {
        if (te.getRotation().getAxis() == EnumFacing.Axis.Y) {
            IBlockState fixState = te.getWorld().getBlockState(te.getPos()).withProperty(BlockPneumaticCraft.ROTATION, EnumFacing.NORTH);
            te.getWorld().setBlockState(te.getPos(), fixState);
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
        private final T handler;

        private ConnectionEntry(String id, Object texture, Capability<T> cap, T handler) {
            this.id = id;
            this.texture = texture;
            this.cap = cap;
            this.handler = handler;
        }
    }
}
