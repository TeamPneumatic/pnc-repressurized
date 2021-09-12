package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import net.minecraft.util.text.IFormattableTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Special "empty" options page for the case where there are no upgrade screens at all.
 * Should not normally happen since core components page should always be visible, but
 * just to be safe...
 */
public class NullOptions implements IOptionPage {
    private static NullOptions instance = null;
    private final IGuiScreen screen;

    public NullOptions(IGuiScreen screen) {
        this.screen = screen;
    }

    public static NullOptions get(GuiArmorMainScreen guiArmorMainScreen) {
        if (instance == null) {
            instance = new NullOptions(guiArmorMainScreen);
        }
        return instance;
    }

    @Override
    public IGuiScreen getGuiScreen() {
        return screen;
    }

    @Override
    public IFormattableTextComponent getPageName() {
        return xlate("pneumaticcraft.armor.gui.misc.noUpgrades");
    }

    @Override
    public void populateGui(IGuiScreen gui) {
    }

    @Override
    public void renderPre(MatrixStack matrixStack, int x, int y, float partialTicks) {
    }

    @Override
    public void renderPost(MatrixStack matrixStack, int x, int y, float partialTicks) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public boolean displaySettingsHeader() {
        return false;
    }
}
