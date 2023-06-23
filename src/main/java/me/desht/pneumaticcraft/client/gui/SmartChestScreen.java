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

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.RangeManager;
import me.desht.pneumaticcraft.common.block.entity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.block.entity.SmartChestBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.SmartChestBlockEntity.PushPullMode;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.inventory.SmartChestMenu;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSmartChest;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.block.entity.SmartChestBlockEntity.CHEST_SIZE;
import static me.desht.pneumaticcraft.common.inventory.SmartChestMenu.N_COLS;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SmartChestScreen extends AbstractPneumaticCraftContainerScreen<SmartChestMenu, SmartChestBlockEntity> {
    private List<Pair<Integer, ItemStack>> filter;
    private IGuiAnimatedStat statusStat;
    private WidgetButtonExtended showRangeButton;

    public SmartChestScreen(SmartChestMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        this.imageWidth = 234;
        this.imageHeight = 216;
        this.filter = te.getFilter();
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.smart_chest.slots.title"), Textures.GUI_MOUSE_LOCATION, 0xFF0090D0, true)
                .setText(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.smart_chest.slots"));

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.SMART_CHEST.get()), 0xFFFFAA00, false);

        addPushPullTab();

        showRangeButton = new WidgetButtonExtended(leftPos + 196, topPos + 189, 12, 12, "A", b -> previewRange());
        addRenderableWidget(showRangeButton);
    }

    private void previewRange() {
        if (AreaRenderManager.getInstance().isShowing(te)) {
            AreaRenderManager.getInstance().removeHandlers(te);
        } else {
            if (te.getUpgrades(ModUpgrades.MAGNET.get()) > 0) {
                int range = te.getUpgrades(ModUpgrades.RANGE.get()) + 1;
                Set<BlockPos> posSet = new HashSet<>();
                for (RelativeFace face : RelativeFace.values()) {
                    if (te.getPushPullMode(face) == PushPullMode.PULL) {
                        Direction dir = te.getAbsoluteFacing(face, te.getRotation());
                        BlockPos pos = te.getBlockPos().relative(dir, range + 1);
                        posSet.addAll(RangeManager.getFrame(new AABB(pos, pos).inflate(range)));
                    }
                }
                AreaRenderManager.getInstance().showArea(posSet, 0x4000FFFF, te, false);
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        List<Component> text = new ArrayList<>();
        text.add(xlate("pneumaticcraft.gui.tab.smartChestStatus.header"));
        text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.smartChestStatus.itemsPerOperation", te.getMaxItems()));
        text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.smartChestStatus.tickInterval", te.getTickRate()));
        statusStat.setText(text);

        if (te.getUpgrades(ModUpgrades.MAGNET.get()) > 0) {
            showRangeButton.setVisible(true);
            if (AreaRenderManager.getInstance().isShowing(te)) {
                showRangeButton.setMessage(Component.literal("R").withStyle(ChatFormatting.AQUA));
                showRangeButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.stopShowingArea"));
            } else {
                showRangeButton.setMessage(Component.literal("R").withStyle(ChatFormatting.GRAY));
                showRangeButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showArea"));
            }
        } else {
            showRangeButton.setVisible(false);
        }
    }

    private void addPushPullTab() {
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.smart_chest.push_pull.title"), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFF90C0E0, false);
        stat.setMinimumExpandedDimensions(80, 80);

        int yTop = 15, xLeft = 25;
        stat.addSubWidget(makePushPullButton(RelativeFace.TOP, xLeft + 22, yTop));
        stat.addSubWidget(makePushPullButton(RelativeFace.LEFT, xLeft, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.FRONT, xLeft + 22, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.RIGHT, xLeft + 44, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.BOTTOM, xLeft + 22, yTop + 44));
        stat.addSubWidget(makePushPullButton(RelativeFace.BACK, xLeft + 44, yTop + 44));
    }

    private WidgetButtonExtended makePushPullButton(RelativeFace face, int x, int y) {
        WidgetButtonExtended button = new WidgetButtonExtended(x, y, 20, 20, Component.empty(), b -> {
            te.cycleMode(face, Screen.hasShiftDown());
            setupPushPullButton((WidgetButtonExtended) b, face);
        }).withTag("push_pull:" + face.toString());
        setupPushPullButton(button, face);
        return button;
    }

    private void setupPushPullButton(WidgetButtonExtended button, RelativeFace face) {
        PushPullMode mode = te.getPushPullMode(face);
        switch (mode) {
            case NONE -> button.setRenderedIcon(Textures.GUI_X_BUTTON);
            case PUSH -> button.setRenderStacks(new ItemStack(Blocks.PISTON));
            case PULL -> button.setRenderStacks(new ItemStack(Blocks.STICKY_PISTON));
        }
        button.setTooltipText(ImmutableList.of(
                Component.literal(face.toString()).withStyle(ChatFormatting.YELLOW),
                xlate(mode.getTranslationKey()))
        );
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SMART_CHEST;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return true;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTick) {
        super.render(matrixStack, x, y, partialTick);

        if (menu.getCarried().isEmpty()
                && hoveredSlot != null
                && hoveredSlot.getItem().isEmpty()
                && hoveredSlot.index < CHEST_SIZE
                && !te.getFilter(hoveredSlot.index).isEmpty())
        {
            ItemStack stack = te.getFilter(hoveredSlot.index);
            List<FormattedCharSequence> l = GuiUtils.wrapTextComponentList(
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.smart_chest.filter", stack.getHoverName().getString(), stack.getCount()),
                    imageWidth, font);
            renderTooltip(matrixStack, l, x, y);
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        super.renderBg(matrixStack, partialTicks, x, y);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // the filtered slots
        for (Pair<Integer, ItemStack> p : filter) {
            int slot = p.getLeft();
            if (slot < te.getLastSlot() && menu.slots.get(slot).hasItem()) {
                int sx = leftPos + 8 + (slot % N_COLS) * 18;
                int sy = topPos + 18 + (slot / N_COLS) * 18;
                fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x8080D080);
            }
        }

        // the closed-off slots
        for (int slot = te.getLastSlot(); slot < CHEST_SIZE; slot++) {
            int sx = leftPos + 8 + (slot % N_COLS) * 18;
            int sy = topPos + 18 + (slot / N_COLS) * 18;
            fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x40FF6060);
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (Pair<Integer, ItemStack> p : filter) {
            int slot = p.getLeft();
            if (slot < te.getLastSlot()) {
                int sx = 8 + (slot % N_COLS) * 18;
                int sy = 18 + (slot / N_COLS) * 18;
                matrixStack.pushPose();
                ItemStack stack = p.getRight();
                Minecraft.getInstance().getItemRenderer().renderGuiItem(matrixStack, stack, sx, sy);
                String label = "[" + stack.getCount() + "]";
                matrixStack.translate(0, 0, 300);
                if (!menu.slots.get(slot).hasItem()) {
                    fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x6080D080);
                }
                matrixStack.scale(0.5f, 0.5f, 0.5f);
                font.drawShadow(matrixStack, label, 2 * (sx + 16 - font.width(label) / 2f), 2 * (sy + 1), 0xFFFFFFA0);
                matrixStack.scale(2.0f, 2.0f, 2.0f);
                matrixStack.popPose();
            }
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotIn != null && slotId < CHEST_SIZE && mouseButton == 0 && Screen.hasAltDown()) {
            ItemStack stack = slotIn.getItem();
            if (stack.isEmpty() && slotId > 0 && te.getFilter(slotId).isEmpty()) {
                if (menu.getCarried().isEmpty()) {
                    // alt-click an empty slot - try to mark this as the last slot
                    // but only if all slots after this are also currently empty
                    if (slotId == te.getLastSlot()) {
                        // re-open all closed slots
                        te.setLastSlot(CHEST_SIZE);
                    } else {
                        // close all slots from the clicked one onwards, if possible
                        for (int i = slotId; i < CHEST_SIZE; i++) {
                            if (!menu.slots.get(i).getItem().isEmpty()) return;
                        }
                        te.setLastSlot(slotId);
                    }
                } else {
                    // alt-click an empty slot with item on cursor: try to set it as a filter
                    ItemStack inHand = menu.getCarried().copy();
                    if (hasShiftDown()) inHand.setCount(inHand.getMaxStackSize());
                    te.setFilter(slotId, inHand);
                    if (te.getLastSlot() <= slotId) {
                        te.setLastSlot(slotId + 1);
                    }
                    this.filter = te.getFilter();
                }
                NetworkHandler.sendToServer(new PacketSyncSmartChest(this.te));
            } else {
                // alt-click an item - toggle filtering for it
                if (te.getFilter(slotId).isEmpty()) {
                    te.setFilter(slotId, hasShiftDown() ? ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize()) : stack);
                } else {
                    te.setFilter(slotId, ItemStack.EMPTY);
                }
                this.filter = te.getFilter();
                NetworkHandler.sendToServer(new PacketSyncSmartChest(this.te));
            }
        } else {
            super.slotClicked(slotIn, slotId, mouseButton, type);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dir) {
        Slot s = getSlotUnderMouse();
        if (Screen.hasAltDown() && s != null && s.index < CHEST_SIZE) {
            ItemStack stack = te.getFilter(s.index);
            if (!stack.isEmpty()) {
                int newSize = hasShiftDown() ?
                        (dir > 0 ? stack.getCount() * 2 : stack.getCount() / 2) :
                        stack.getCount() + (int) dir;
                newSize = Mth.clamp(newSize, 1, stack.getMaxStackSize());
                if (newSize != stack.getCount()) {
                    te.setFilter(s.index, ItemHandlerHelper.copyStackWithSize(stack, newSize));
                    this.filter = te.getFilter();
                    sendDelayed(5);  // avoid packet spam while spinning mouse wheel
                }
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, dir);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Slot s = getSlotUnderMouse();
        if (Screen.hasAltDown() && s != null && s.index < CHEST_SIZE) {
            ItemStack stack = te.getFilter(s.index);
            if (!stack.isEmpty()) {
                int newCount = switch (keyCode) {
                    case GLFW.GLFW_KEY_UP -> Screen.hasShiftDown() ? stack.getCount() * 2 : stack.getCount() + 1;
                    case GLFW.GLFW_KEY_DOWN -> Screen.hasShiftDown() ? stack.getCount() / 2 : stack.getCount() - 1;
                    default -> stack.getCount();
                };
                newCount = Mth.clamp(newCount, 1, stack.getMaxStackSize());
                if (newCount != stack.getCount()) {
                    te.setFilter(s.index, ItemHandlerHelper.copyStackWithSize(stack, newCount));
                    this.filter = te.getFilter();
                    sendDelayed(5);  // avoid packet spam while spinning mouse wheel
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void doDelayedAction() {
        NetworkHandler.sendToServer(new PacketSyncSmartChest(this.te));
    }
}
