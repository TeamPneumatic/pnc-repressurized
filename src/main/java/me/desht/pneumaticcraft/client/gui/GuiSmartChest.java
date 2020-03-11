package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
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
import me.desht.pneumaticcraft.common.network.PacketSmartChestSync;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest.PushPullMode;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.inventory.ContainerSmartChest.N_COLS;
import static me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest.CHEST_SIZE;

public class GuiSmartChest extends GuiPneumaticContainerBase<ContainerSmartChest, TileEntitySmartChest> {
    private List<Pair<Integer, Item>> filter;
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

        addAnimatedStat("gui.tab.info.smart_chest.slots.title", Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("gui.tab.info.smart_chest.slots");

        statusStat = addAnimatedStat("gui.tab.status", new ItemStack(ModBlocks.SMART_CHEST.get()), 0xFFFFAA00, false);

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
                showRangeButton.setMessage(TextFormatting.AQUA + "A");
                showRangeButton.setTooltipText(I18n.format("gui.programmer.button.stopShowingArea"));
            } else {
                showRangeButton.setMessage(TextFormatting.GRAY + "A");
                showRangeButton.setTooltipText(I18n.format("gui.programmer.button.showArea"));
            }
        } else {
            showRangeButton.setVisible(false);
        }
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<>();
        textList.add(I18n.format("gui.tab.smartChestStatus.header"));
        textList.add(I18n.format("gui.tab.smartChestStatus.itemsPerOperation", te.getMaxItems()));
        textList.add(I18n.format("gui.tab.smartChestStatus.tickInterval", te.getTickRate()));
        return textList;
    }

    private void addPushPullTab() {
        WidgetAnimatedStat stat = addAnimatedStat("gui.tab.info.smart_chest.push_pull.title", new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFF90C0E0, false);
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
                button.setRenderStacks(ItemStack.EMPTY);
                break;
            case PUSH:
                button.setRenderStacks(new ItemStack(Blocks.PISTON));
                break;
            case PULL:
                button.setRenderStacks(new ItemStack(Blocks.STICKY_PISTON));
                break;
        }
        button.setTooltipText(ImmutableList.of(TextFormatting.YELLOW + face.toString(), I18n.format(mode.getTranslationKey())));
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
    public void render(int x, int y, float partialTick) {
        super.render(x, y, partialTick);

        if (minecraft.player.inventory.getItemStack().isEmpty()
                && hoveredSlot != null
                && hoveredSlot.getStack().isEmpty()
                && hoveredSlot.slotNumber < CHEST_SIZE
                && !te.getFilter(hoveredSlot.slotNumber).isEmpty())
        {
            List<String> l = Lists.newArrayList(TextFormatting.GRAY + "Filter",
                    te.getFilter(hoveredSlot.slotNumber).getDisplayName().getFormattedText());
            renderTooltip(l, x, y);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        GlStateManager.enableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (Pair<Integer, Item> p : filter) {
            int slot = p.getLeft();
            if (slot < te.getLastSlot()) {
                int sx = 8 + (slot % N_COLS) * 18;
                int sy = 18 + (slot / N_COLS) * 18;
                if (container.inventorySlots.get(slot).getStack().isEmpty()) {
                    ItemStack stack = new ItemStack(p.getRight());
                    GuiUtils.drawItemStack(stack, sx, sy);
                    GlStateManager.translated(0, 0, 300);
                }
                fill(sx, sy, sx + 16, sy + 16, 0x8080D080);
                if (container.inventorySlots.get(slot).getStack().isEmpty()) {
                    GlStateManager.translated(0, 0, -300);
                }
            }
        }

        super.drawGuiContainerForegroundLayer(x, y);

        for (int slot = te.getLastSlot(); slot < CHEST_SIZE; slot++) {
            int sx = 8 + (slot % N_COLS) * 18;
            int sy = 18 + (slot / N_COLS) * 18;
            fill(sx, sy, sx + 16, sy + 16, 0x40FF6060);
        }
        GlStateManager.disableBlend();
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
                NetworkHandler.sendToServer(new PacketSmartChestSync(this.te));
            } else {
                // alt-click an item - toggle filtering for it
                if (te.getFilter(slotId).isEmpty()) {
                    te.setFilter(slotId, stack);
                } else {
                    te.setFilter(slotId, ItemStack.EMPTY);
                }
                this.filter = te.getFilter();
                NetworkHandler.sendToServer(new PacketSmartChestSync(this.te));
            }
        } else {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }
}
