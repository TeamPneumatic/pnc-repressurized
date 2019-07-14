package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.AirConUpgradeHandler;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import net.minecraft.client.Minecraft;

public class GuiAirConditionerOptions extends IOptionPage.SimpleToggleableOptions {

    public GuiAirConditionerOptions(AirConUpgradeHandler airConUpgradeHandler) {
        super(airConUpgradeHandler);
    }

    @Override
    public void initGui(IGuiScreen gui) {
        super.initGui(gui);

        gui.getWidgetList().add(new GuiButtonSpecial(30, 128, 150, 20, "Move Stat Screen...", b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getRenderHandler(), ArmorHUDLayout.LayoutTypes.AIR_CON));
        }));
    }
}
