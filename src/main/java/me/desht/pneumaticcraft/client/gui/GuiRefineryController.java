package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiRefineryController extends GuiPneumaticContainerBase<ContainerRefinery, TileEntityRefineryController> {
    private List<TileEntityRefineryOutput> outputs;
    private WidgetTemperature widgetTemperature;
    private int nExposedFaces;

    public GuiRefineryController(ContainerRefinery container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        widgetTemperature = new WidgetTemperature(guiLeft + 32, guiTop + 20, 273, 673, te.getHeatExchangerLogic(null)) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);
                if (te.minTemp > 0) {
                    TextFormatting tf = te.minTemp < te.getHeatExchangerLogic(null).getTemperatureAsInt() ? TextFormatting.GREEN : TextFormatting.GOLD;
                    curTip.add(tf + "Required Temperature: " + (te.minTemp - 273) + "\u00b0C");
                }
            }
        };
        addButton(widgetTemperature);

        addButton(new WidgetTank(guiLeft + 8, guiTop + 13, te.getInputTank()));

        int x = guiLeft + 95;
        int y = guiTop + 17;

        // "te" always refers to the master refinery; the bottom block of the stack
        outputs = new ArrayList<>();
//        refineries.add(te);
        TileEntity te1 = te.findAdjacentOutput();
        if (te1 instanceof TileEntityRefineryOutput) {
            int i = 0;
            do {
                TileEntityRefineryOutput teRO = (TileEntityRefineryOutput) te1;
                if (outputs.size() < 4) addButton(new WidgetTank(x, y, te.outputsSynced[i++]));
                x += 20;
                y -= 4;
                outputs.add(teRO);
                te1 = te1.getWorld().getTileEntity(te1.getPos().up());
            } while (te1 instanceof TileEntityRefineryOutput);

//            addButton(new WidgetTank(x, y, te1.getOutputTank()));
//            outputs.add(te1);
//            TileEntity te1 = te.getWorld().getTileEntity(te1.getPos().up();
//            while (te1.getTileCache()[Direction.UP.ordinal()].getTileEntity() instanceof TileEntityRefineryOutput) {
//                te1 = (TileEntityRefineryOutput) te1.getTileCache()[Direction.UP.ordinal()].getTileEntity();
//                x += 20;
//                y -= 4;
//                if (outputs.size() < 4) addButton(new WidgetTank(x, y, te1.getOutputTank()));
//                outputs.add(te1);
//            }
        }

        if (outputs.size() < 2 || outputs.size() > 4) {
            problemTab.openWindow();
        }

        nExposedFaces = HeatUtil.countExposedFaces(outputs);
    }

    @Override
    public void tick() {
        super.tick();

        if (te.minTemp > 0) {
            widgetTemperature.setScales(te.minTemp);
        } else {
            widgetTemperature.setScales();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if (outputs.size() < 4) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            fill(guiLeft + 155, guiTop + 5, guiLeft + 171, guiTop + 69, 0x40FF0000);
            if (outputs.size() < 3) {
                fill(guiLeft + 135, guiTop + 9, guiLeft + 151, guiTop + 73, 0x40FF0000);
            }
            if (outputs.size() < 2) {
                fill(guiLeft + 115, guiTop + 13, guiLeft + 131, guiTop + 77, 0x40FF0000);
            }
            if (outputs.size() < 1) {
                fill(guiLeft + 95, guiTop + 17, guiLeft + 111, guiTop + 81, 0x40FF0000);
            }
            GlStateManager.disableBlend();
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REFINERY;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(-36, 0);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(20, -1);
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (te.getHeatExchangerLogic(null).getTemperatureAsInt() < te.minTemp) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
        if (te.getInputTank().getFluidAmount() < 10) {
            curInfo.add("gui.tab.problems.refinery.noOil");
        }
        if (outputs.size() < 2) {
            curInfo.add("gui.tab.problems.refinery.notEnoughRefineries");
        } else if (outputs.size() > 4) {
            curInfo.add("gui.tab.problems.refinery.tooManyRefineries");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (te.isBlocked()) {
            curInfo.add("gui.tab.problems.refinery.outputBlocked");
        }
        if (nExposedFaces > 0) {
            curInfo.add(I18n.format("gui.tab.problems.exposedFaces", nExposedFaces, outputs.size() * 6));
        }
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }
}
