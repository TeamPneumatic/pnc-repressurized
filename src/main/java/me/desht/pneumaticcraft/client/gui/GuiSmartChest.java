package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerSmartChest;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSmartChest;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest.PushPullMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.inventory.ContainerSmartChest.N_COLS;
import static me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest.CHEST_SIZE;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiSmartChest extends GuiPneumaticContainerBase<ContainerSmartChest, TileEntitySmartChest> {
    private List<Pair<Integer, ItemStack>> filter;
    private IGuiAnimatedStat statusStat;
    private WidgetButtonExtended showRangeButton;

    public GuiSmartChest(ContainerSmartChest container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        this.xSize = 234;
        this.ySize = 216;
        this.filter = te.getFilter();
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.smart_chest.slots.title"), Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("pneumaticcraft.gui.tab.info.smart_chest.slots");

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.SMART_CHEST.get()), 0xFFFFAA00, false);

        addPushPullTab();

        showRangeButton = new WidgetButtonExtended(guiLeft + 196, guiTop + 189, 12, 12, "A", b -> showRangeLines());
        addButton(showRangeButton);
    }

    private void showRangeLines() {
        if (AreaRenderManager.getInstance().isShowing(te)) {
            AreaRenderManager.getInstance().removeHandlers(te);
        } else {
            if (te.getUpgrades(EnumUpgrade.MAGNET) > 0) {
                int range = te.getUpgrades(EnumUpgrade.RANGE) + 1;
                Set<BlockPos> posSet = new HashSet<>();
                for (RelativeFace face : RelativeFace.values()) {
                    if (te.getPushPullMode(face) == PushPullMode.PULL) {
                        Direction dir = te.getAbsoluteFacing(face, te.getRotation());
                        BlockPos p = te.getPos().offset(dir, range + 1);
                        for (int x = -range; x <= range; x++) {
                            for (int y = -range; y <= range; y++) {
                                for (int z = -range; z <= range; z++) {
                                    posSet.add(p.add(x, y, z));
                                }
                            }
                        }
                    }
                }
                AreaRenderManager.getInstance().showArea(posSet, 0x6000FFFF, te);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        statusStat.setText(getStatus());

        if (te.getUpgrades(EnumUpgrade.MAGNET) > 0) {
            showRangeButton.setVisible(true);
            if (AreaRenderManager.getInstance().isShowing(te)) {
                showRangeButton.setMessage(new StringTextComponent("A").mergeStyle(TextFormatting.AQUA));
                showRangeButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.stopShowingArea"));
            } else {
                showRangeButton.setMessage(new StringTextComponent("A").mergeStyle(TextFormatting.GRAY));
                showRangeButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showArea"));
            }
        } else {
            showRangeButton.setVisible(false);
        }
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<>();
        textList.add(I18n.format("pneumaticcraft.gui.tab.smartChestStatus.header"));
        textList.add(I18n.format("pneumaticcraft.gui.tab.smartChestStatus.itemsPerOperation", te.getMaxItems()));
        textList.add(I18n.format("pneumaticcraft.gui.tab.smartChestStatus.tickInterval", te.getTickRate()));
        return textList;
    }

    private void addPushPullTab() {
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.smart_chest.push_pull.title"), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFF90C0E0, false);
        stat.addPadding(7, 16);

        int yTop = 15, xLeft = 25;
        stat.addSubWidget(makePushPullButton(RelativeFace.TOP, xLeft + 22, yTop));
        stat.addSubWidget(makePushPullButton(RelativeFace.LEFT, xLeft, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.FRONT, xLeft + 22, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.RIGHT, xLeft + 44, yTop + 22));
        stat.addSubWidget(makePushPullButton(RelativeFace.BOTTOM, xLeft + 22, yTop + 44));
        stat.addSubWidget(makePushPullButton(RelativeFace.BACK, xLeft + 44, yTop + 44));
    }

    private WidgetButtonExtended makePushPullButton(RelativeFace face, int x, int y) {
        WidgetButtonExtended button = new WidgetButtonExtended(x, y, 20, 20, "", b -> {
            te.cycleMode(face);
            setupPushPullButton((WidgetButtonExtended) b, face);
        }).withTag("push_pull:" + face.toString());
        setupPushPullButton(button, face);
        return button;
    }

    private void setupPushPullButton(WidgetButtonExtended button, RelativeFace face) {
        PushPullMode mode = te.getPushPullMode(face);
        switch (mode) {
            case NONE:
                button.setRenderedIcon(Textures.GUI_X_BUTTON);
                break;
            case PUSH:
                button.setRenderStacks(new ItemStack(Blocks.PISTON));
                break;
            case PULL:
                button.setRenderStacks(new ItemStack(Blocks.STICKY_PISTON));
                break;
        }
        button.setTooltipText(ImmutableList.of(
                new StringTextComponent(face.toString()).mergeStyle(TextFormatting.YELLOW),
                xlate(mode.getTranslationKey()))
        );
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SMART_CHEST;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTick) {
        super.render(matrixStack, x, y, partialTick);

        if (minecraft.player.inventory.getItemStack().isEmpty()
                && hoveredSlot != null
                && hoveredSlot.getStack().isEmpty()
                && hoveredSlot.slotNumber < CHEST_SIZE
                && !te.getFilter(hoveredSlot.slotNumber).isEmpty())
        {
            ItemStack stack = te.getFilter(hoveredSlot.slotNumber);
            List<ITextComponent> l = PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.smart_chest.filter",
                    stack.getDisplayName().getString(), stack.getCount()), 40);
            renderTooltip(matrixStack, l, x, y);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, x, y);

        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // the filtered slots
        for (Pair<Integer, ItemStack> p : filter) {
            int slot = p.getLeft();
            if (slot < te.getLastSlot() && container.inventorySlots.get(slot).getHasStack()) {
                int sx = guiLeft + 8 + (slot % N_COLS) * 18;
                int sy = guiTop + 18 + (slot / N_COLS) * 18;
                fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x8080D080);
            }
        }

        // the closed-off slots
        for (int slot = te.getLastSlot(); slot < CHEST_SIZE; slot++) {
            int sx = guiLeft + 8 + (slot % N_COLS) * 18;
            int sy = guiTop + 18 + (slot / N_COLS) * 18;
            fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x40FF6060);
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);

        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (Pair<Integer, ItemStack> p : filter) {
            int slot = p.getLeft();
            if (slot < te.getLastSlot()) {
                int sx = 8 + (slot % N_COLS) * 18;
                int sy = 18 + (slot / N_COLS) * 18;
                matrixStack.push();
                ItemStack stack = p.getRight();
                GuiUtils.renderItemStack(matrixStack, stack, sx, sy);
                String label = "[" + stack.getCount() + "]";
                matrixStack.translate(0, 0, 300);
                if (!container.inventorySlots.get(slot).getHasStack()) {
                    fill(matrixStack, sx, sy, sx + 16, sy + 16, 0x8080D080);
                }
                matrixStack.scale(0.5f, 0.5f, 0.5f);
                font.drawString(matrixStack, label, 2 * (sx + 16 - font.getStringWidth(label) / 2f), 2 * (sy + 1), 0xFFFFFFA0);
                matrixStack.scale(2.0f, 2.0f, 2.0f);
                matrixStack.pop();
            }
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotId < CHEST_SIZE && mouseButton == 0 && Screen.hasAltDown()) {
            ItemStack stack = slotIn.getStack();
            if (stack.isEmpty() && slotId > 0 && te.getFilter(slotId).isEmpty()) {
                // alt-click an empty slot - try to mark this as the last slot
                // but only if all slots after this are also currently empty
                if (slotId == te.getLastSlot()) {
                    te.setLastSlot(CHEST_SIZE);
                } else {
                    for (int i = slotId; i < CHEST_SIZE; i++) {
                        if (!container.inventorySlots.get(i).getStack().isEmpty()) return;
                    }
                    te.setLastSlot(slotId);
                }
                NetworkHandler.sendToServer(new PacketSyncSmartChest(this.te));
            } else {
                // alt-click an item - toggle filtering for it
                if (te.getFilter(slotId).isEmpty()) {
                    te.setFilter(slotId, stack);
                } else {
                    te.setFilter(slotId, ItemStack.EMPTY);
                }
                this.filter = te.getFilter();
                NetworkHandler.sendToServer(new PacketSyncSmartChest(this.te));
            }
        } else {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dir) {
        Slot s = getSlotUnderMouse();
        if (Screen.hasAltDown() && s != null && s.slotNumber < CHEST_SIZE) {
            ItemStack stack = te.getFilter(s.slotNumber);
            if (!stack.isEmpty()) {
                int newSize = hasShiftDown() ?
                        (dir > 0 ? stack.getCount() * 2 : stack.getCount() / 2) :
                        stack.getCount() + (int) dir;
                newSize = MathHelper.clamp(newSize, 1, stack.getMaxStackSize());
                if (newSize != stack.getCount()) {
                    te.setFilter(s.slotNumber, ItemHandlerHelper.copyStackWithSize(stack, newSize));
                    this.filter = te.getFilter();
                    sendDelayed(5);  // avoid packet spam while spinning mouse wheel
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Slot s = getSlotUnderMouse();
        if (Screen.hasAltDown() && s != null && s.slotNumber < CHEST_SIZE) {
            ItemStack stack = te.getFilter(s.slotNumber);
            if (!stack.isEmpty()) {
                int newSize = stack.getCount();
                switch (keyCode) {
                    case GLFW.GLFW_KEY_UP:
                        newSize = Screen.hasShiftDown() ? newSize * 2 : newSize + 1;
                        break;
                    case GLFW.GLFW_KEY_DOWN:
                        newSize = Screen.hasShiftDown() ? newSize / 2 : newSize - 1;
                        break;
                }
                newSize = MathHelper.clamp(newSize, 1, stack.getMaxStackSize());
                if (newSize != stack.getCount()) {
                    te.setFilter(s.slotNumber, ItemHandlerHelper.copyStackWithSize(stack, newSize));
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
