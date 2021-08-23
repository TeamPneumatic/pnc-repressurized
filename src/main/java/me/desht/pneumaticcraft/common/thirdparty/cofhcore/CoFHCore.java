package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.item.IPressurizableItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CoFHCore implements IThirdParty {
    static Enchantment holdingEnchantment = null;
    private static boolean versionOK;

    @Override
    public void preInit() {
        // FIXME bit of a hack here, but we need to be sure we have a compatible version of CoFH Core
        // should be able to get rid of this once CoFH Core has a public API
        versionOK = versionOK();
    }

    @Override
    public void init() {
        // note: fuel registration is now by datapack: all done by conditional recipes in ModRecipeProvider

        if (versionOK) {
            // holding enchantment adds another volume multiplier
            holdingEnchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("cofh_core", "holding"));
            if (holdingEnchantment != null) {
                PneumaticRegistry.getInstance().getItemRegistry().registerPneumaticVolumeModifier(
                        (stack, oldVolume) -> oldVolume * (1 + EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack))
                );
            }
        }
    }

    public static int getHoldingUpgrades(ItemStack stack) {
        return holdingEnchantment == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack);
    }

    public static ITextComponent holdingEnchantmentName(int level) {
        return holdingEnchantment == null ? StringTextComponent.EMPTY : holdingEnchantment.getFullname(level);
    }

    private static boolean versionOK() {
        try {
            Class.forName("cofh.lib.capability.IEnchantableItem");
            return true;
        } catch (ClassNotFoundException e) {
            Log.error("PneumaticCraft: Repressurized found an older (pre-1.2.0) release of CoFH Core. Continuing, but PneumaticCraft items won't be able to use the Holding enchantment. Upgrade to CoFH Core 1.2.0 or later if possible.");
            return false;
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void attachCap(AttachCapabilitiesEvent<ItemStack> event) {
            // potentially allow any pressurizable items to take the CoFH holding enchantment
            if (versionOK && holdingEnchantment != null && HoldingEnchantableProvider.CAPABILITY_ENCHANTABLE_ITEM != null
                    && event.getObject().getItem() instanceof IPressurizableItem) {
                event.addCapability(RL("cofh_enchantable"), new HoldingEnchantableProvider());
            }
        }

    }
}
