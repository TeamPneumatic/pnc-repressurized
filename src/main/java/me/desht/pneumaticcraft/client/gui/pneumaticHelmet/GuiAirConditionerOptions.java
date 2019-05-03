package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.AirConUpgradeHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GuiAirConditionerOptions extends IOptionPage.SimpleToggleableOptions {

    public GuiAirConditionerOptions(AirConUpgradeHandler airConUpgradeHandler) {
        super(airConUpgradeHandler);
    }

    @Override
    public void initGui(IGuiScreen gui) {
        super.initGui(gui);

        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Stat Screen..."));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            FMLClientHandler.instance().getClient().player.closeScreen();
            FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(getRenderHandler()));
        }
    }

}
