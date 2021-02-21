package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiRefineryController extends GuiPneumaticContainerBase<ContainerRefinery, TileEntityRefineryController> {
    private List<TileEntityRefineryOutput> outputs;
    private WidgetTemperature widgetTemperature;
    private int nExposedFaces;

    public GuiRefineryController(ContainerRefinery container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 189;
    }

    @Override
    public void init() {
        super.init();

        widgetTemperature = new WidgetTemperature(guiLeft + 32, guiTop + 32, TemperatureRange.of(273, 673), 273, 50);
        addButton(widgetTemperature);

        addButton(new WidgetTank(guiLeft + 8, guiTop + 25, te.getInputTank()));

        int x = guiLeft + 95;
        int y = guiTop + 29;

        // "te" always refers to the master refinery; the bottom block of the stack
        outputs = new ArrayList<>();
        TileEntity te1 = te.findAdjacentOutput();
        if (te1 != null) {
            int i = 0;
            do {
                TileEntityRefineryOutput teRO = (TileEntityRefineryOutput) te1;
                if (outputs.size() < 4) addButton(new WidgetTank(x, y, te.outputsSynced[i++]));
                x += 20;
                y -= 4;
                outputs.add(teRO);
                te1 = te1.getWorld().getTileEntity(te1.getPos().up());
            } while (te1 instanceof TileEntityRefineryOutput);
        }

        if (outputs.size() < 2 || outputs.size() > 4) {
            problemTab.openStat();
        }

        nExposedFaces = HeatUtil.countExposedFaces(outputs);
    }

    @Override
    public void tick() {
        super.tick();

        if (te.maxTemp > te.minTemp) {
            widgetTemperature.setOperatingRange(TemperatureRange.of(te.minTemp, te.maxTemp));
        } else {
            widgetTemperature.setOperatingRange(null);
        }
        widgetTemperature.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        widgetTemperature.autoScaleForTemperature();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, f, x, y);
        if (outputs.size() < 4) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            fill(matrixStack, guiLeft + 155, guiTop + 17, guiLeft + 171, guiTop + 81, 0x40FF0000);
            if (outputs.size() < 3) {
                fill(matrixStack, guiLeft + 135, guiTop + 21, guiLeft + 151, guiTop + 85, 0x40FF0000);
            }
            if (outputs.size() < 2) {
                fill(matrixStack, guiLeft + 115, guiTop + 25, guiLeft + 131, guiTop + 89, 0x40FF0000);
            }
            if (outputs.size() < 1) {
                fill(matrixStack, guiLeft + 95, guiTop + 29, guiLeft + 111, guiTop + 93, 0x40FF0000);
            }
            RenderSystem.disableBlend();
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REFINERY;
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.getHeatExchanger().getTemperatureAsInt() < te.minTemp) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.notEnoughHeat"));
        }
        if (te.getInputTank().getFluidAmount() < 10) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.noOil"));
        }
        if (outputs.size() < 2) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.notEnoughRefineries"));
        } else if (outputs.size() > 4) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.tooManyRefineries"));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);

        if (te.isBlocked()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.outputBlocked"));
        }
        if (nExposedFaces > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, outputs.size() * 6));
        }
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }
}
