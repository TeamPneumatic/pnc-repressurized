package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiRefinery extends GuiPneumaticContainerBase<TileEntityRefinery> {
    private List<TileEntityRefinery> refineries;

    public GuiRefinery(InventoryPlayer player, TileEntityRefinery te) {
        super(new ContainerRefinery(player, te), te, Textures.GUI_REFINERY);
    }

    @Override
    public void initGui() {
        super.initGui();

        addWidget(new WidgetTemperature(-1, guiLeft + 32, guiTop + 20, 273, 673, te.getHeatExchangerLogic(null), 373));

        addWidget(new WidgetTank(-1, guiLeft + 8, guiTop + 13, te.getInputTank()));

        int x = guiLeft + 95;
        int y = guiTop + 17;
        addWidget(new WidgetTank(-1, x, y, te.getOutputTank()));

        refineries = new ArrayList<>();
        refineries.add(te);
        TileEntityRefinery refinery = te;
        while (refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity();
            x += 20;
            y -= 4;
            if (refineries.size() < 4) addWidget(new WidgetTank(-1, x, y, refinery.getOutputTank()));
            refineries.add(refinery);
        }

        if (refineries.size() < 2 || refineries.size() > 4) {
            problemTab.openWindow();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if (refineries.size() < 4) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(guiLeft + 155, guiTop + 5, guiLeft + 171, guiTop + 69, 0x40FF0000);
            if (refineries.size() < 3) {
                drawRect(guiLeft + 135, guiTop + 9, guiLeft + 151, guiTop + 73, 0x40FF0000);
            }
            if (refineries.size() < 2) {
                drawRect(guiLeft + 115, guiTop + 13, guiLeft + 131, guiTop + 77, 0x40FF0000);
            }
            GlStateManager.disableBlend();
        }
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(-36, 0);
    }

    @Override
    protected Point getInvTextOffset() {
        return new Point(20, -1);
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getHeatExchangerLogic(null).getTemperature() < 395) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
        if (te.getInputTank().getFluidAmount() < 10) {
            curInfo.add("gui.tab.problems.refinery.noOil");
        }
        if (refineries.size() < 2) {
            curInfo.add("gui.tab.problems.refinery.notEnoughRefineries");
        } else if (refineries.size() > 4) {
            curInfo.add("gui.tab.problems.refinery.tooManyRefineries");
        } else if (te.isBlocked()) {
            curInfo.add("gui.tab.problems.refinery.outputBlocked");
        }
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }
}
