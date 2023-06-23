/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.MinigunMagazineMenu;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class MinigunMagazineScreen extends AbstractPneumaticCraftContainerScreen<MinigunMagazineMenu, AbstractPneumaticCraftBlockEntity> implements IExtraGuiHandling {
    private int lockedSlot = -1;

    public MinigunMagazineScreen(MinigunMagazineMenu container, Inventory inv, Component displayString) {
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
    public void containerTick() {
        super.containerTick();

        ItemStack gunStack = Minecraft.getInstance().player.getItemInHand(menu.getHand());
        if (gunStack.getItem() instanceof MinigunItem) {
            lockedSlot = MinigunItem.getLockedSlot(gunStack);
        }
    }

    @Override
    public void drawExtras(ContainerScreenEvent.Render.Foreground event) {
        if (lockedSlot >= 0) {
            // highlight the locked slot with a semitransparent green tint
            int minX = 26 + (lockedSlot % 2) * 18;
            int minY = 26 + (lockedSlot / 2) * 18;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            PoseStack matrixStack = event.getPoseStack();
            BufferBuilder wr = Tesselator.getInstance().getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            GuiUtils.drawUntexturedQuad(matrixStack, wr, minX, minY, 0, 16, 16, 0, 208, 0, 50);
            RenderSystem.lineWidth(3.0F);
            GuiUtils.drawOutline(matrixStack, wr, minX, minY, 0, 16, 16, 0, 208, 0, 255);
            RenderSystem.lineWidth(1.0F);
        }
    }
}
