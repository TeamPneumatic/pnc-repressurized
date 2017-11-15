package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.util.List;

/**
 * Just an interface to give access to GuiSreen#buttonList and GuiScreen#fontRenderer. An instance of this class can
 * safely be casted to GuiSreen if needed.
 */
public interface IGuiScreen {
    List<GuiButton> getButtonList();

    FontRenderer getFontRenderer();
}
