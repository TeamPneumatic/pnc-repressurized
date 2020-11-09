package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.inventory.ContainerFluidTank;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiFluidTank extends GuiPneumaticContainerBase<ContainerFluidTank, TileEntityFluidTank> {
    public GuiFluidTank(ContainerFluidTank container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 152, guiTop + 15, te.getTank()));
    }

    @Override
    protected String upgradeCategory() {
        return "fluid_tank";
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_FLUID_TANK;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
}
