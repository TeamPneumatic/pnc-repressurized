package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerInventorySearcher;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class GuiInventorySearcher extends ContainerScreen<ContainerInventorySearcher> {
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private final Screen parentScreen;
    private Predicate<ItemStack> stackPredicate = itemStack -> true;
    private WidgetLabel label;
    private int clickedMouseButton;

    public GuiInventorySearcher(ContainerInventorySearcher container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);

        inv.player.openContainer = container;
        passEvents = true;
        ySize = 176;
        parentScreen = Minecraft.getInstance().currentScreen;
        container.init(inventory);
    }

    @Override
    protected void init() {
        super.init();

        addButton(label = new WidgetLabel(guiLeft + 105, guiTop + 28, "", 0xFF404080));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        minecraft.keyboardListener.enableRepeatEvents(false);
        if (parentScreen != null) {
            minecraft.displayGuiScreen(parentScreen);
            if (parentScreen instanceof ContainerScreen) {
                minecraft.player.openContainer = ((ContainerScreen) parentScreen).getContainer();
            }
        } else {
            super.onClose();
        }
    }

    public void setStackPredicate(Predicate<ItemStack> predicate) {
        stackPredicate = predicate;
    }

    @Nonnull
    public ItemStack getSearchStack() {
        return inventory.getStackInSlot(0);
    }

    public void setSearchStack(@Nonnull ItemStack stack) {
        if (!stack.isEmpty() && stackPredicate.test(stack)) {
            inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (slot != null) {
            if (slot.slotNumber == 36) {
                clickedMouseButton = 0;
                slot.putStack(ItemStack.EMPTY);
            } else {
                clickedMouseButton = mouseButton;
                setSearchStack(slot.getStack());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (inventory.getStackInSlot(0).getItem() instanceof IPositionProvider) {
            label.setMessage(PneumaticCraftUtils.posToString(getBlockPos()));
        } else {
            label.setMessage("");
        }
    }

    /**
     * Special case for when the searched item is a position provider
     * @return the selected blockpos, or null if the search item is not a position provider
     */
    @Nonnull
    public BlockPos getBlockPos() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider) {
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getRawStoredPositions(ClientUtils.getClientWorld(), stack);
            int posIdx = getPosIdx(stack);
            if (!posList.isEmpty()) {
                return posList.get(Math.min(posIdx, posList.size() - 1));
            }
        }
        return BlockPos.ZERO;
    }

    private int getPosIdx(ItemStack stack) {
        if (stack.getItem() instanceof ItemGPSAreaTool) {
            // for gps area tool, RMB is idx 0, LMB is idx 1
            switch (clickedMouseButton) {
                case 0: return 1;  // LMB
                case 1: return 0;  // RMB
                default: return 1;  // any other button
            }
        } else {
            return clickedMouseButton;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        renderBackground();
        minecraft.getTextureManager().bindTexture(Textures.GUI_INVENTORY_SEARCHER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(xStart, yStart, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        int x = (xSize - font.getStringWidth(getTitle().getFormattedText())) / 2;
        font.drawString(getTitle().getFormattedText(), x, 5, 0x404040);

        // darken out all non-matching slots
        for (int i = 0; i < this.container.inventorySlots.size() - 1; ++i) {
            Slot slot = this.container.inventorySlots.get(i);
            if (!stackPredicate.test(slot.getStack())) {
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0xC0202020, 0xC0202020);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }

    @Override
    public void render(int par1, int par2, float par3) {
        super.render(par1, par2, par3);

        if (this.hoveredSlot != null && stackPredicate.test(this.hoveredSlot.getStack())) {
            renderHoveredToolTip(par1, par2);
        }
    }
}
