package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetList;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerTagWorkbench;
import me.desht.pneumaticcraft.common.item.ItemTagFilter;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTagWorkbench;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiTagWorkbench extends GuiPneumaticContainerBase<ContainerTagWorkbench, TileEntityTagWorkbench> {
    private static final int AVAILABLE_X = 9;
    private static final int SELECTED_X = 123;
    private static final int LIST_HEIGHT = 126;
    private static final int LIST_WIDTH = 98;
    private static final int LIST_Y = 41;

    private Item lastItem = null;
    private ItemStack lastPaperStack = ItemStack.EMPTY;

    private WidgetButtonExtended addButton;
    private WidgetButtonExtended removeButton;
    private WidgetList<ResourceLocation> availableList;
    private WidgetList<ResourceLocation> selectedList;
    private WidgetButtonExtended writeButton;

    public GuiTagWorkbench(ContainerTagWorkbench container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        xSize = 234;
        ySize = 256;
    }

    @Override
    public void init() {
        super.init();

        addButton(writeButton = new WidgetButtonExtended(guiLeft + 162, guiTop + 16, 20, 20, "", b -> writeTags())
                .setRenderStacks(new ItemStack(Items.WRITABLE_BOOK)).setTooltipText(I18n.format("pneumaticcraft.gui.tooltip.tag_workbench.write_button")));
        addButton(addButton = new WidgetButtonExtended(guiLeft + 108, guiTop + 90, 13, 13, GuiConstants.TRIANGLE_RIGHT,
                b -> addAvailable()));
        addButton(removeButton = new WidgetButtonExtended(guiLeft + 108, guiTop + 106, 13, 13, GuiConstants.TRIANGLE_LEFT,
                b -> removeSelected()));

        addButton(availableList = new WidgetList<>(guiLeft + AVAILABLE_X, guiTop + LIST_Y, LIST_WIDTH, LIST_HEIGHT, this::onSelected));
        addButton(selectedList = new WidgetList<>(guiLeft + SELECTED_X, guiTop + LIST_Y, LIST_WIDTH, LIST_HEIGHT, this::onSelected));
    }

    private void writeTags() {
        List<String> l = selectedList.getLines().stream().map(ResourceLocation::toString).collect(Collectors.toList());
        NetworkHandler.sendToServer(new PacketGuiButton("write:" + String.join("," , l)));
        selectedList.clear();
    }

    private void onSelected(WidgetList<ResourceLocation> w) {
        if (w == availableList) {
            selectedList.unselectAll();
            if (w.isDoubleClicked()) addAvailable();
        } else if (w == selectedList) {
            availableList.unselectAll();
            if (w.isDoubleClicked()) removeSelected();
        }
    }

    private void addAvailable() {
        ResourceLocation rl = availableList.getSelectedLine();
        if (rl != null && !selectedList.contains(rl)) {
            selectedList.add(rl);
        }
    }

    private void removeSelected() {
        ResourceLocation rl = selectedList.getSelectedLine();
        if (rl != null) {
            selectedList.removeSelected();
        }
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack stack = container.getSlot(0).getStack();
        if (stack.getItem() != lastItem) {
            availableList.clear();
            stack.getItem().getTags().forEach(rl -> availableList.add(rl));
            availableList.unselectAll();
            lastItem = stack.getItem();
        }
        ItemStack stack1 = container.getSlot(1).getStack();
        if (!ItemStack.areItemStacksEqual(stack1, lastPaperStack)) {
            if (stack1.getItem() == ModItems.TAG_FILTER.get()) {
                Set<ResourceLocation> s = ItemTagFilter.getConfiguredTagList(stack1);
                s.addAll(selectedList.getLines());
                selectedList.clear();
                s.forEach(rl -> selectedList.add(rl));
            }
            selectedList.unselectAll();
            lastPaperStack = stack1.copy();
        }

        addButton.active = availableList.getSelectedLine() != null;
        removeButton.active = selectedList.getSelectedLine() != null;
        writeButton.active = selectedList.size() > 0
                && (!container.getSlot(TileEntityTagWorkbench.PAPER_SLOT).getStack().isEmpty()
                || !container.getSlot(TileEntityTagWorkbench.OUTPUT_SLOT).getStack().isEmpty());
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_TAG_WORKBENCH;
    }
}
