package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiMinigunMagazine extends GuiPneumaticContainerBase<ContainerMinigunMagazine,TileEntityBase> implements IExtraGuiHandling {
    private int lockedSlot = -1;

    public GuiMinigunMagazine(ContainerMinigunMagazine container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addInfoTab(GuiUtils.xlateAndSplit("gui.tooltip.item.pneumaticcraft.minigun"));
        addAnimatedStat(xlate("pneumaticcraft.gui.tab.minigun.slotInfo.title"), new ItemStack(ModItems.GUN_AMMO.get()), 0xFF0080C0, true)
                .setText(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.minigun.slotInfo"));
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

        ItemStack gunStack = ItemMinigun.getHeldMinigun(Minecraft.getInstance().player);
        if (gunStack.getItem() instanceof ItemMinigun) {
            lockedSlot = ItemMinigun.getLockedSlot(gunStack);
        }
    }

    @Override
    public void drawExtras(GuiContainerEvent.DrawForeground event) {
        if (lockedSlot >= 0) {
            // highlight the locked slot with a semitransparent green tint
            int minX = 26 + (lockedSlot % 2) * 18;
            int minY = 26 + (lockedSlot / 2) * 18;

            RenderSystem.disableTexture();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            MatrixStack matrixStack = event.getMatrixStack();
            BufferBuilder wr = Tessellator.getInstance().getBuffer();
            GuiUtils.drawUntexturedQuad(matrixStack, wr, minX, minY, 0, 16, 16, 0, 208, 0, 50);
            RenderSystem.lineWidth(3.0F);
            GuiUtils.drawOutline(matrixStack, wr, minX, minY, 0, 16, 16, 0, 208, 0, 255);
            RenderSystem.lineWidth(1.0F);

            RenderSystem.enableTexture();
        }
    }
}
