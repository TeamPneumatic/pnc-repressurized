package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class Mekanism implements IThirdParty {
    @Override
    public void init() {
        MekanismIntegration.mekSetup();
    }

    @SubscribeEvent
    public static void attachHeatAdapters(AttachCapabilitiesEvent<TileEntity> event) {
        if (PNCConfig.Common.Integration.mekThermalEfficiencyFactor != 0 && MekanismIntegration.CAPABILITY_HEAT_HANDLER != null) {
            if (event.getObject().getType().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
                event.addCapability(RL("pnc2mek_heat_adapter"), new PNC2MekHeatProvider(event.getObject()));
            }
            if (event.getObject().getType().getRegistryName().getNamespace().equals(ModIds.MEKANISM)) {
                event.addCapability(RL("mek2pnc_heat_adapter"), new Mek2PNCHeatProvider(event.getObject()));
            }
        }
    }

    @SubscribeEvent
    public static void attachRadiationShield(AttachCapabilitiesEvent<ItemStack> event) {
        if (MekanismIntegration.CAPABILITY_RADIATION_SHIELDING != null) {
            if (event.getObject().getItem() instanceof ItemPneumaticArmor) {
                event.addCapability(RL("mek_rad_shielding"),
                        new MekRadShieldProvider(event.getObject(),
                                ((ItemPneumaticArmor) event.getObject().getItem()).getSlot())
                );
            }
        }
    }
}
