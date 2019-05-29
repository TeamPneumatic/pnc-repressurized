package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStation;
import me.desht.pneumaticcraft.common.item.IChargingStationGUIHolderItem;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiChargingStation extends GuiPneumaticContainerBase<TileEntityChargingStation> {
    private GuiButtonSpecial guiSelectButton;
    private float renderAirProgress;

    public GuiChargingStation(InventoryPlayer player, TileEntityChargingStation te) {
        super(new ContainerChargingStation(player, te), te, Textures.GUI_CHARGING_STATION_LOCATION);

        ySize = 176;
    }

    @Override
    public void initGui() {
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new GuiButtonSpecial(1, xStart + 89, yStart + 15, 21, 20, "");
        guiSelectButton.setRenderedIcon(Textures.GUI_UPGRADES_LOCATION);
        buttonList.add(guiSelectButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 46, 19, 4210752);
    }

    @Override
    protected Point getInvTextOffset() {
        return new Point(0, 3);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderAir();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ItemStack stack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        guiSelectButton.visible = stack.getItem() instanceof IChargingStationGUIHolderItem;
        if (guiSelectButton.visible) {
            guiSelectButton.setTooltipText("Manage upgrades for the " + stack.getDisplayName());
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
    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 10, yStart + ySize / 4 + 4);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.charging || te.discharging) {
            pressureStatText.add("\u00a77" + (te.charging ? "C" : "Disc") + "harging at");
            pressureStatText.add("\u00a70" + (double) Math.round(PneumaticValues.CHARGING_STATION_CHARGE_RATE * te.getSpeedMultiplierFromUpgrades()) + "mL/tick");
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        if (!chargeStack.isEmpty() && !(chargeStack.getItem() instanceof IPressurizable)) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The inserted item can't be (dis)charged", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        if (chargeStack.isEmpty()) {
            curInfo.add("\u00a7fNo items to (dis)charge");
            curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
        } else if (chargeStack.getItem() instanceof IPressurizable) {
            String name = chargeStack.getDisplayName();
            IPressurizable chargeItem = (IPressurizable) chargeStack.getItem();
            if (chargeItem.getPressure(chargeStack) > te.getPressure() + 0.01F && chargeItem.getPressure(chargeStack) <= 0) {
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a7fThe " + name + " can't be discharged", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                curInfo.add("\u00a70The item is empty.");
            } else if (chargeItem.getPressure(chargeStack) < te.getPressure() - 0.01F && chargeItem.getPressure(chargeStack) >= chargeItem.maxPressure(chargeStack)) {
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a7fThe " + name + " can't be charged", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                curInfo.add("\u00a70The item is full.");
            } else if (!te.charging && !te.discharging) {
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a7fThe " + name + " can't be (dis)charged", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                curInfo.add("\u00a70The pressures have equalized.");
            }
        }
    }

    private void renderAir() {
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.glLineWidth(2.0F);
        int particles = 10;
        for (int i = 0; i < particles; i++) {
            renderAirParticle(renderAirProgress % (1F / particles) + (float) i / particles);
        }

        GlStateManager.enableTexture2D();
    }

    private void renderAirParticle(float particleProgress) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        float x = xStart + 117F;
        float y = yStart + 50.5F;
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
        wr.pos(x, y, zLevel).endVertex();
        Tessellator.getInstance().draw();
    }
}
