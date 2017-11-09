package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModInteractionUtils {
    private static final ModInteractionUtils INSTANCE = new ModInteractionUtilImplementation();

    @GameRegistry.ObjectHolder("thermalfoundation:wrench")
    private static final Item CRESCENT_HAMMER = null;
    @GameRegistry.ObjectHolder("rftools:smartwrench")
    private static final Item SMART_WRENCH = null;
    // TODO add other modded wrenches here, and detect them in setupWrenchItems() below

    // list is probably fine here, since the number of members is very small
    // maybe use a set if we end up with a large number of wrench items?
    private static final List<Item> wrenchItems = new ArrayList<>();

    public static void setupWrenchItems() {
        if (CRESCENT_HAMMER != null) wrenchItems.add(CRESCENT_HAMMER);
        if (SMART_WRENCH != null) wrenchItems.add(SMART_WRENCH);
    }

    public static ModInteractionUtils getInstance() {
        return INSTANCE;
    }

    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return wrenchItems.contains(stack.getItem());
    }

    public boolean isModdedWrench(Item item) {
        return wrenchItems.contains(item);
    }

    /**
     * Used to get a IPneumaticMachine if the TE is one. When FMP is installed this will also look for a multipart containing IPneumaticMachine.
     *
     * @param te
     * @return
     */
    public IPneumaticMachine getMachine(TileEntity te) {
        return te instanceof IPneumaticMachine ? (IPneumaticMachine) te : null;
    }

    public IPneumaticWrenchable getWrenchable(TileEntity te) {
        return null;
    }

    public Item getModuleItem(String moduleName) {
        return new ItemTubeModule(moduleName);
    }

    public void registerModulePart(String partName) {
    }

    public boolean isMultipart(TileEntity te) {
        return false;
    }

    public ItemStack exportStackToTEPipe(TileEntity te, ItemStack stack, EnumFacing side) {
        return stack;
    }

    public ItemStack exportStackToBCPipe(TileEntity te, ItemStack stack, EnumFacing side) {
        return stack;
    }

    public boolean isBCPipe(TileEntity te) {
        return false;
    }

    public boolean isTEPipe(TileEntity te) {
        return false;
    }

    public boolean isMultipartWiseConnected(Object part, EnumFacing dir) {
        return false;
    }

    /**
     * @param potentialTube Either a TileMultipart, PartPressureTube or TileEntityPressureTube
     * @return
     */
    public TileEntityPressureTube getTube(Object potentialTube) {
        return potentialTube instanceof TileEntityPressureTube ? (TileEntityPressureTube) potentialTube : null;
    }

    public void sendDescriptionPacket(IPneumaticPosProvider te) {
        ((TileEntityPressureTube) te).sendDescriptionPacket();
    }

    public void removeTube(TileEntity te) {
        te.getWorld().setBlockToAir(te.getPos());
    }

    public boolean occlusionTest(AxisAlignedBB aabb, TileEntity te) {
        return true;
    }
}
