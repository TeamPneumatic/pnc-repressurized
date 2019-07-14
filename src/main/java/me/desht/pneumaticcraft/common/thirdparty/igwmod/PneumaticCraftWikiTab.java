package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.gui.GuiWiki;
import igwmod.gui.tabs.BaseWikiTab;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class PneumaticCraftWikiTab extends BaseWikiTab {

    public PneumaticCraftWikiTab() {
        pageEntries.add("base_concepts");
        pageEntries.add("generating_pressure");
        pageEntries.add("heat");
        pageEntries.add("pressure_tubes");
        pageEntries.add("pressure_chamber");
        pageEntries.add("machine_upgrades");
        pageEntries.add("block/pneumatic_dynamo");
        skipLine();
        pageEntries.add("block/omnidirectional_hopper");
        pageEntries.add("block/liquid_hopper");
        pageEntries.add("block/air_cannon");
        pageEntries.add("pneumatic_door");
        pageEntries.add("block/charging_station");
        skipLine();
        pageEntries.add("oil");
        pageEntries.add("block/plastic_mixer");
        pageEntries.add("elevator");
        pageEntries.add("block/universal_sensor");
        pageEntries.add("item/logistics_module");
        pageEntries.add("item/logistic_drone");
        skipLine();
        pageEntries.add("pneumatic_armor");
        pageEntries.add("block/programmer");
        pageEntries.add("item/drone");
        pageEntries.add("block/programmable_controller");
        pageEntries.add("item/remote");
        skipLine();
        pageEntries.add("printed_circuit_boards");
        pageEntries.add("assembly_machines");
        pageEntries.add("block/aphorism_tile");
        pageEntries.add("block/security_station");
        pageEntries.add("block/aerial_interface");
        skipLine();
        pageEntries.add("ic2integration");
        pageEntries.add("cofh_integration");
        pageEntries.add("cc_integration");

    }

    @Override
    public String getName() {
        return Names.MOD_NAME;
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui) {
        return new ItemStack(ModBlocks.AIR_CANNON);
    }

    @Override
    protected String getPageName(String pageEntry) {
        if (pageEntry.startsWith("item") || pageEntry.startsWith("block")) {
            return I18n.format(pageEntry.replace("/", ".").replace("block", "tile") + ".name");
        } else {
            return I18n.format("igwtab.entry." + pageEntry);
        }
    }

    @Override
    protected String getPageLocation(String pageEntry) {
        if (pageEntry.startsWith("item") || pageEntry.startsWith("block")) return "pneumaticcraft:" + pageEntry;
        return "pneumaticcraft:menu/" + pageEntry;
    }

}
