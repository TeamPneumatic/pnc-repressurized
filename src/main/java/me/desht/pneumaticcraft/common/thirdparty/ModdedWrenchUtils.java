package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.item.ItemPneumaticWrench;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public enum ModdedWrenchUtils {
    INSTANCE;

    private final Set<ResourceLocation> wrenches = new HashSet<>();

    public static ModdedWrenchUtils getInstance() {
        return INSTANCE;
    }

    public void registerThirdPartyWrenches() {
        // some well-known wrenches. item tag "forge:tools/wrench" can also be used to detect a wrench item
        registerWrench("thermalfoundation:wrench");
        registerWrench("rftools:smartwrench");
        registerWrench("immersiveengineering:hammer");
        registerWrench("appliedenergistics2:certus_quartz_wrench");
        registerWrench("appliedenergistics2:nether_quartz_wrench");
        registerWrench("enderio:item_yeta_wrench");
        registerWrench("buildcraftcore:wrench");
        registerWrench("teslacorelib:wrench");
        registerWrench("ic2:wrench");
        registerWrench("chiselsandbits:wrench_wood");
        registerWrench("mekanism:configurator");
    }

    private void registerWrench(String wrenchId) {
        wrenches.add(new ResourceLocation(wrenchId));
    }
    
    /**
     * Check if the given item is a known 3rd party modded wrench (does not include our own Pneumatic Wrench)
     *
     * @param stack the item to check
     * @return true if it's a modded wrench, false otherwise
     */
    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof ItemPneumaticWrench) &&
                (stack.getItem().is(PneumaticCraftTags.Items.WRENCHES) || wrenches.contains(stack.getItem().getRegistryName()));
    }

    /**
     * Check if the given item is *any* known wrench item, including our own Pneumatic Wrench
     *
     * @param stack the item to check
     * @return true if it's a wrench, false otherwise
     */
    public boolean isWrench(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemPneumaticWrench || isModdedWrench(stack);
    }
}
