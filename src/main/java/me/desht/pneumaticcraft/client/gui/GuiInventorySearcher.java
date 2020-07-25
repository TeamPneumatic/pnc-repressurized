package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerInventorySearcher;
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
import net.minecraft.util.text.StringTextComponent;
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

        addButton(label = new WidgetLabel(guiLeft + 105, guiTop + 28, StringTextComponent.EMPTY, 0xFF404080));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void closeScreen() {
        minecraft.keyboardListener.enableRepeatEvents(false);
        if (parentScreen != null) {
            minecraft.displayGuiScreen(parentScreen);
            if (parentScreen instanceof ContainerScreen) {
                minecraft.player.openContainer = ((ContainerScreen) parentScreen).getContainer();
            }
        } else {
            super.closeScreen();
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
    protected void handleMouseClick(Slot par1Slot, int par2, int par3, ClickType par4) {
        if (par1Slot != null) {
            if (par1Slot.slotNumber == 36) {
                par1Slot.putStack(ItemStack.EMPTY);
            } else {
                setSearchStack(par1Slot.getStack());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        label.setMessage(StringTextComponent.EMPTY);
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider) {
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(ClientUtils.getClientWorld(), stack);
            if (!posList.isEmpty()) {
                BlockPos pos = posList.get(0);
                if (pos != null) {
                    label.setMessage(new StringTextComponent(PneumaticCraftUtils.posToString(pos)));
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float par1, int par2, int par3) {
        renderBackground(matrixStack);
        minecraft.getTextureManager().bindTexture(Textures.GUI_INVENTORY_SEARCHER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(matrixStack, xStart, yStart, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        font.func_238422_b_(matrixStack, getTitle(), this.width / 2f, 5, 0x404040);

        // darken out all non-matching slots
        for (int i = 0; i < this.container.inventorySlots.size() - 1; ++i) {
            Slot slot = this.container.inventorySlots.get(i);
            if (!stackPredicate.test(slot.getStack())) {
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(matrixStack, slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0xC0202020, 0xC0202020);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int par1, int par2, float par3) {
        super.render(matrixStack, par1, par2, par3);

        if (this.hoveredSlot != null && stackPredicate.test(this.hoveredSlot.getStack())) {
            func_230459_a_(matrixStack, par1, par2);  // renderHoveredTooltip
        }
    }
}
