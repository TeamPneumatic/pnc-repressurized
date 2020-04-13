package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;
import org.lwjgl.opengl.GL11;

public class GuiMinigunMagazine extends GuiPneumaticContainerBase<ContainerMinigunMagazine,TileEntityBase> implements IExtraGuiHandling {
    private int lockedSlot = -1;

    public GuiMinigunMagazine(ContainerMinigunMagazine container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addInfoTab("gui.tooltip.item.pneumaticcraft.minigun");
        addAnimatedStat("gui.tab.minigun.slotInfo.title", new ItemStack(ModItems.GUN_AMMO.get()), 0xFF0080C0, true).setText("gui.tab.minigun.slotInfo");
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
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_MINIGUN_MAGAZINE;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack gunStack = minecraft.player.getHeldItemMainhand();
        if (gunStack.getItem() instanceof ItemMinigun) {
            if (NBTUtil.hasTag(gunStack, ItemMinigun.NBT_LOCKED_SLOT)) {
                lockedSlot = NBTUtil.getInteger(gunStack, ItemMinigun.NBT_LOCKED_SLOT);
            } else {
                lockedSlot = -1;
            }
        }
    }

    @Override
    public void drawExtras(GuiContainerEvent.DrawForeground event) {
        if (lockedSlot >= 0) {
            // highlight the locked slot with a semitransparent green tint
            int minX = 26 + (lockedSlot % 2) * 18;
            int minY = 26 + (lockedSlot / 2) * 18;
            RenderSystem.lineWidth(3.0F);
            RenderSystem.disableTexture();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            RenderSystem.color4f(0f, 0.8f, 0f, 0.2f);
            BufferBuilder wr = Tessellator.getInstance().getBuffer();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            wr.pos(minX, minY, 0.0).endVertex();
            wr.pos(minX, minY + 16, 0.0).endVertex();
            wr.pos(minX + 16, minY + 16, 0.0).endVertex();
            wr.pos(minX + 16, minY, 0.0).endVertex();
            Tessellator.getInstance().draw();

            RenderSystem.color4f(0f, 0.8f, 0f, 1);
            wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            wr.pos(minX, minY, 0.0).endVertex();
            wr.pos(minX, minY + 16, 0.0).endVertex();
            wr.pos(minX + 16, minY + 16, 0.0).endVertex();
            wr.pos(minX + 16, minY, 0.0).endVertex();
            Tessellator.getInstance().draw();
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableTexture();
        }
    }
}
