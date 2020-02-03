package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.event.ClientTickHandler;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnectionHandler implements INeedTickUpdate {
    protected final GuiSecurityStationBase gui;
    protected final TileEntitySecurityStation station;
    private int baseX;
    private int baseY;
    private final int nodeSpacing;
    protected final int color;
    final List<RenderProgressingLine> lineList = new ArrayList<>();
    final boolean[] slotHacked = new boolean[35];
    final boolean[] slotFortified = new boolean[35];
    private final float baseBridgeSpeed;

    NetworkConnectionHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
                             int baseY, int nodeSpacing, int color, float baseBridgeSpeed) {
        this.gui = gui;
        this.station = station;
        this.baseX = baseX;
        this.baseY = baseY;
        this.nodeSpacing = nodeSpacing;
        this.color = color;
        this.baseBridgeSpeed = baseBridgeSpeed;
        ClientTickHandler.instance().registerUpdatedObject(this);
    }

    NetworkConnectionHandler(NetworkConnectionHandler copy) {
        this(copy.gui, copy.station, copy.baseX, copy.baseY, copy.nodeSpacing, copy.color, copy.baseBridgeSpeed);
        for (int i = 0; i < slotHacked.length; i++) {
            slotHacked[i] = copy.slotHacked[i];
            slotFortified[i] = copy.slotFortified[i];
        }
        for (RenderProgressingLine line : copy.lineList) {
            lineList.add(new RenderProgressingLine(line));
        }
    }

    /**
     * Constructor used when resolution gets updated.
     *
     * @param copy object to copy
     * @param baseX
     * @param baseY
     */
    NetworkConnectionHandler(NetworkConnectionHandler copy, int baseX, int baseY) {
        this(copy);
        this.baseX = baseX;
        this.baseY = baseY;
        for (RenderProgressingLine line : lineList) { //adjust the copied lines for the new baseX and baseY
            line.startX = line.startX - copy.baseX + baseX;
            line.startY = line.startY - copy.baseY + baseY;
            line.endX = line.endX - copy.baseX + baseX;
            line.endY = line.endY - copy.baseY + baseY;
        }
    }

    public void render() {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(f1, f2, f3, f);
        for (RenderProgressingLine line : lineList) {
            line.render();
        }
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    @Override
    public void update() {
        for (RenderProgressingLine line : lineList) {
            int slot = line.getPointedSlotNumber(gui);
            ItemStack stack = station.getPrimaryInventory().getStackInSlot(slot);
            boolean done = line.incProgress(baseBridgeSpeed * (1 / (TileEntityConstants.NETWORK_NOTE_RATING_MULTIPLIER * (stack.isEmpty() ? 1 : stack.getCount() + (slotFortified[slot] ? 1 : 0)))));
            if (done) {
                if (slot < slotHacked.length) {
                    if (!slotHacked[slot]) onSlotHack(slot, false);
                    slotHacked[slot] = true;
                }
            }
        }
    }

    protected void onSlotHack(int slot, boolean nuked) {
    }

    void addConnection(int firstSlot, int secondSlot) {
        double startX = baseX + firstSlot % 5.0 * nodeSpacing;
        double startY = baseY + firstSlot / 5.0 * nodeSpacing;
        double endX = baseX + secondSlot % 5.0 * nodeSpacing;
        double endY = baseY + secondSlot / 5.0 * nodeSpacing;
        for (RenderProgressingLine line : lineList) {
            if (line.hasLineSameProperties(startX, startY, 0, endX, endY, 0)) return;
        }
        lineList.add(new RenderProgressingLine(startX, startY, endX, endY));
    }

    void removeConnection(int firstSlot, int secondSlot) {
        double startX = baseX + firstSlot % 5.0 * nodeSpacing;
        double startY = baseY + firstSlot / 5.0 * nodeSpacing;
        double endX = baseX + secondSlot % 5.0 * nodeSpacing;
        double endY = baseY + secondSlot / 5.0 * nodeSpacing;
        for (RenderProgressingLine line : lineList) {
            if (line.hasLineSameProperties(startX, startY, 0, endX, endY, 0)) {
                lineList.remove(line);
                return;
            }
        }
    }

    boolean tryToHackSlot(int slotNumber) {
        boolean successfullyHacked = false;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (station.connects(slotNumber, slotNumber + i + j * 5) && slotHacked[slotNumber + i + j * 5]) {
                    addConnection(slotNumber + i + j * 5, slotNumber);
                    successfullyHacked = true;
                }
            }
        }
        return successfullyHacked;
    }

    boolean canHackSlot(int slotNumber) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (station.connects(slotNumber, slotNumber + i + j * 5) && slotHacked[slotNumber + i + j * 5]) {
                    return true;
                }
            }
        }
        return false;
    }
}
