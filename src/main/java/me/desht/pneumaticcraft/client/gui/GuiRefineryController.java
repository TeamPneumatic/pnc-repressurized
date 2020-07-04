package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
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

//        widgetTemperature = new WidgetTemperature(guiLeft + 32, guiTop + 32, 273, 673,
//                te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY))
//        {
//            @Override
//            public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
//                super.addTooltip(mouseX, mouseY, curTip, shift);
//                if (te.minTemp > 0) {
//                    int temp = logic.map(IHeatExchangerLogic::getTemperatureAsInt).orElseThrow(RuntimeException::new);
//                    TextFormatting tf = te.minTemp < temp ? TextFormatting.GREEN : TextFormatting.GOLD;
//                    curTip.add(tf + I18n.format("pneumaticcraft.gui.misc.requiredTemperature", te.minTemp - 273));
//                }
//            }
//        };
        widgetTemperature = new WidgetTemperature(guiLeft + 32, guiTop + 32, TemperatureRange.of(273, 673), 273, 50);
        addButton(widgetTemperature);

        addButton(new WidgetTank(guiLeft + 8, guiTop + 25, te.getInputTank()));

        int x = guiLeft + 95;
        int y = guiTop + 29;

        // "te" always refers to the master refinery; the bottom block of the stack
        outputs = new ArrayList<>();
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
        }

        if (outputs.size() < 2 || outputs.size() > 4) {
            problemTab.openWindow();
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
        te.getHeatCap(null).ifPresent(l -> widgetTemperature.setTemperature(l.getTemperatureAsInt()));
        widgetTemperature.autoScaleForTemperature();

//        if (te.minTemp > 0) {
//            widgetTemperature.setScales(te.minTemp);
//        } else {
//            widgetTemperature.setScales();
//        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if (outputs.size() < 4) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            fill(guiLeft + 155, guiTop + 17, guiLeft + 171, guiTop + 81, 0x40FF0000);
            if (outputs.size() < 3) {
                fill(guiLeft + 135, guiTop + 21, guiLeft + 151, guiTop + 85, 0x40FF0000);
            }
            if (outputs.size() < 2) {
                fill(guiLeft + 115, guiTop + 25, guiLeft + 131, guiTop + 89, 0x40FF0000);
            }
            if (outputs.size() < 1) {
                fill(guiLeft + 95, guiTop + 29, guiLeft + 111, guiTop + 93, 0x40FF0000);
            }
            RenderSystem.disableBlend();
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REFINERY;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        int temp = te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                .map(IHeatExchangerLogic::getTemperatureAsInt).orElseThrow(RuntimeException::new);
        if (temp < te.minTemp) {
            curInfo.add("pneumaticcraft.gui.tab.problems.notEnoughHeat");
        }
        if (te.getInputTank().getFluidAmount() < 10) {
            curInfo.add("pneumaticcraft.gui.tab.problems.refinery.noOil");
        }
        if (outputs.size() < 2) {
            curInfo.add("pneumaticcraft.gui.tab.problems.refinery.notEnoughRefineries");
        } else if (outputs.size() > 4) {
            curInfo.add("pneumaticcraft.gui.tab.problems.refinery.tooManyRefineries");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (te.isBlocked()) {
            curInfo.add("pneumaticcraft.gui.tab.problems.refinery.outputBlocked");
        }
        if (nExposedFaces > 0) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, outputs.size() * 6));
        }
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }
}
