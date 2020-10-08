package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerSpawnerExtractor;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class GuiSpawnerExtractor extends GuiPneumaticContainerBase<ContainerSpawnerExtractor, TileEntitySpawnerExtractor> {
    public GuiSpawnerExtractor(ContainerSpawnerExtractor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ELEVATOR;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4 + 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);

        int progress = MathHelper.clamp((int)(te.getProgress() * 100f), 0, 100);
        font.drawString(matrixStack, "Progress:", 65, 35, 0x404040);
        font.drawString(matrixStack, progress + "%", 80, 47, 0x404040);
    }
}
