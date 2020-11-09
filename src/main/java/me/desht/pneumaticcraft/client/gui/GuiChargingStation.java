package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStation;
import me.desht.pneumaticcraft.common.item.IChargeableContainerProvider;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiChargingStation extends GuiPneumaticContainerBase<ContainerChargingStation,TileEntityChargingStation> {
    private WidgetButtonExtended guiSelectButton;
    private float renderAirProgress;

    public GuiChargingStation(ContainerChargingStation container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 182;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new WidgetButtonExtended(xStart + 90, yStart + 22, 18, 19, StringTextComponent.EMPTY).withTag("open_upgrades");
        guiSelectButton.setRenderedIcon(Textures.GUI_UPGRADES_LOCATION);
        guiSelectButton.visible = false;
        addButton(guiSelectButton);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_CHARGING_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, opacity, x, y);

        renderAir(matrixStack);
    }

    @Override
    public void tick() {
        super.tick();
        ItemStack stack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        guiSelectButton.visible = stack.getItem() instanceof IChargeableContainerProvider;
        if (guiSelectButton.visible) {
            guiSelectButton.setTooltipText(xlate("pneumaticcraft.gui.tooltip.charging_station.manageUpgrades", stack.getDisplayName()));
        }

        // multiplier of 25 is about the max that looks good (higher values can make the animation look like
        // it's going the wrong way)
        if (te.charging) {
            renderAirProgress += 0.001F * Math.min(25f, te.getSpeedMultiplierFromUpgrades());
            if (renderAirProgress > 1f) renderAirProgress = 0f;
        } else if (te.discharging) {
            renderAirProgress -= 0.001F * Math.min(25f, te.getSpeedMultiplierFromUpgrades());
            if (renderAirProgress < 0f) renderAirProgress = 1f;
        }
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4 + 10, yStart + ySize / 4 + 10);
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.charging || te.discharging) {
            String key = te.charging ? "pneumaticcraft.gui.tooltip.charging" : "pneumaticcraft.gui.tooltip.discharging";
            String amount = PneumaticCraftUtils.roundNumberTo(PneumaticValues.CHARGING_STATION_CHARGE_RATE * te.getSpeedMultiplierFromUpgrades(), 1);
            pressureStatText.add(xlate(key, amount).mergeStyle(TextFormatting.BLACK));
        } else {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.charging", 0).mergeStyle(TextFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> textList) {
        super.addProblems(textList);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        if (!chargeStack.isEmpty() && !chargeStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent()) {
            // shouldn't ever happen - I can't be bothered to add a translation
            textList.add(new StringTextComponent(TextFormatting.RED + "Non-pneumatic item in the charge slot!?"));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        if (chargeStack.isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.no_item"));
        } else {
            chargeStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
                String name = chargeStack.getDisplayName().getString();
                if (h.getPressure() > te.getPressure() + 0.01F && h.getPressure() <= 0) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.item_empty", name));
                } else if (h.getPressure() < te.getPressure() - 0.01F && h.getPressure() >= h.maxPressure()) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.item_full", name));
                } else if (!te.charging && !te.discharging) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.pressure_equal", name));
                }
            });
        }
    }

    private void renderAir(MatrixStack matrixStack) {
        RenderSystem.disableTexture();
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.lineWidth(2.0F);
        int particles = 10;
        for (int i = 0; i < particles; i++) {
            renderAirParticle(matrixStack, renderAirProgress % (1F / particles) + (float) i / particles);
        }

        RenderSystem.enableTexture();
    }

    private void renderAirParticle(MatrixStack matrixStack, float particleProgress) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        float x = xStart + 117F;
        float y = yStart + 56.5F;
        if (particleProgress < 0.5F) {
            y += particleProgress * 56;
        } else if (particleProgress < 0.7F) {
            y += 28F;
            x -= (particleProgress - 0.5F) * 90;
        } else {
            y += 28F;
            x -= 18;
            y -= (particleProgress - 0.7F) * 70;
        }
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GL11.glPointSize(5);
        wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
        wr.pos(matrixStack.getLast().getMatrix(), x, y, 0f).endVertex();
        Tessellator.getInstance().draw();
    }
}
