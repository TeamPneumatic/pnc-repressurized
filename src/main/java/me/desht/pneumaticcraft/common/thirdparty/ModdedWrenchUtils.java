package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public enum ModdedWrenchUtils {
    INSTANCE;

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

    private final Set<String> wrenches = new HashSet<>();

    public static ModdedWrenchUtils getInstance() {
        return INSTANCE;
    }

    public void registerThirdPartyWrenches() {
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

    private void registerWrench(Item wrench) {
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

}
