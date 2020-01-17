package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiContainerEvent;
import org.lwjgl.opengl.GL11;

public class GuiMinigunMagazine extends GuiPneumaticContainerBase implements IExtraGuiHandling {
    private int lockedSlot = -1;

    public GuiMinigunMagazine(InventoryPlayer inventoryPlayer) {
        super(new ContainerMinigunMagazine(inventoryPlayer.player), null, Textures.GUI_MINIGUN_MAGAZINE);
    }

    @Override
    public void initGui() {
        super.initGui();

        addInfoTab("gui.tooltip.item.minigun");
        addAnimatedStat("gui.tab.minigun.slotInfo.title", new ItemStack(Itemss.GUN_AMMO), 0xFF0080C0, true).setText("gui.tab.minigun.slotInfo");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j) {
        super.drawGuiContainerBackgroundLayer(partialTicks, i, j);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        ItemStack gunStack = ItemMinigun.getHeldMinigun(Minecraft.getMinecraft().player);
        if (gunStack.getItem() instanceof ItemMinigun) {
            lockedSlot = ItemMinigun.getLockedSlot(gunStack);
        }
    }

    @Override
    public void drawExtras(GuiContainerEvent.DrawForeground event) {
        if (lockedSlot >= 0) {
            int minX = 26 + (lockedSlot % 2) * 18;
            int minY = 26 + (lockedSlot / 2) * 18;
            GlStateManager.glLineWidth(3.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.color(0f, 0.8f, 0f, 0.2f);
            BufferBuilder wr = Tessellator.getInstance().getBuffer();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            wr.pos(minX, minY, zLevel).endVertex();
            wr.pos(minX, minY + 16, zLevel).endVertex();
            wr.pos(minX + 16, minY + 16, zLevel).endVertex();
            wr.pos(minX + 16, minY, zLevel).endVertex();
            Tessellator.getInstance().draw();

            GlStateManager.color(0f, 0.8f, 0f, 1);
            wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            wr.pos(minX, minY, zLevel).endVertex();
            wr.pos(minX, minY + 16, zLevel).endVertex();
            wr.pos(minX + 16, minY + 16, zLevel).endVertex();
            wr.pos(minX + 16, minY, zLevel).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.enableTexture2D();
        }
    }
}
