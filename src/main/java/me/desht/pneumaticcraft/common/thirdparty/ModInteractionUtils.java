package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ModInteractionUtils {
    private static final ModInteractionUtils INSTANCE = new ModInteractionUtils();

    @GameRegistry.ObjectHolder("thermalfoundation:wrench")
    private static final Item CRESCENT_HAMMER = null;
    @GameRegistry.ObjectHolder("rftools:smartwrench")
    private static final Item SMART_WRENCH = null;
    @GameRegistry.ObjectHolder("immersiveengineering:tool")
    private static final Item IMMERSIVE_TOOL = null;
    @GameRegistry.ObjectHolder("appliedenergistics2:certus_quartz_wrench")
    private static final Item AE2_CERTUS_WRENCH = null;
    @GameRegistry.ObjectHolder("appliedenergistics2:nether_quartz_wrench")
    private static final Item AE2_NETHER_WRENCH = null;
    @GameRegistry.ObjectHolder("enderio:item_yeta_wrench")
    private static final Item YETA_WRENCH = null;
    @GameRegistry.ObjectHolder("buildcraftcore:wrench")
    private static final Item BC_WRENCH = null;
    @GameRegistry.ObjectHolder("teslacorelib:wrench")
    private static final Item TESLA_WRENCH = null;
    @GameRegistry.ObjectHolder("ic2:wrench")
    private static final Item IC2_WRENCH = null;
    @GameRegistry.ObjectHolder("chiselsandbits:wrench_wood")
    private static final Item CB_WRENCH_WOOD = null;

    private static final Set<String> wrenches = new HashSet<>();

    public static ModInteractionUtils getInstance() {
        return INSTANCE;
    }

    public static void registerThirdPartyWrenches() {
        registerWrench(CRESCENT_HAMMER);
        registerWrench(SMART_WRENCH);
        registerWrench(IMMERSIVE_TOOL);
        registerWrench(AE2_CERTUS_WRENCH);
        registerWrench(AE2_NETHER_WRENCH);
        registerWrench(YETA_WRENCH);
        registerWrench(BC_WRENCH);
        registerWrench(TESLA_WRENCH);
        registerWrench(IC2_WRENCH);
        registerWrench(CB_WRENCH_WOOD);
    }

    private static void registerWrench(Item wrench) {
        if (wrench != null) wrenches.add(makeWrenchKey(new ItemStack(wrench)));
    }

    private static String makeWrenchKey(ItemStack wrench) {
        return wrench.getItem().getRegistryName() + (getWrenchMeta(wrench) >= 0 ? ":" + wrench.getMetadata() : "");
    }

    private static int getWrenchMeta(ItemStack wrench) {
        if (wrench.getItem() == IMMERSIVE_TOOL) return 0;
        return -1;
    }

    /**
     * Check if the given item is a known 3rd party modded wrench
     *
     * @param stack the item to check
     * @return true if it's a modded wrench, false otherwise
     */
    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return wrenches.contains(makeWrenchKey(stack));
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

    public boolean occlusionTest(AxisAlignedBB aabb, TileEntity te) {
        return true;
    }
}
