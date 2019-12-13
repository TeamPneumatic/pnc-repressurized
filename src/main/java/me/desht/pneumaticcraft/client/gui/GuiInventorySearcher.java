package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.inventory.ContainerInventorySearcher;
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
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class GuiInventorySearcher extends ContainerScreen<ContainerInventorySearcher> {
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private final Screen parentScreen;
    private Predicate<ItemStack> stackPredicate = itemStack -> true;

    public GuiInventorySearcher(ContainerInventorySearcher container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);

        inv.player.openContainer = container;
        passEvents = true;
        ySize = 176; //TODO change
        parentScreen = Minecraft.getInstance().currentScreen;
        container.init(inventory);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.displayGuiScreen(parentScreen);
            onClose();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        font.drawString("Inventory", 7, 5, 4210752);
        font.drawString("Searcher", 7, 15, 4210752);
        font.drawString("Target", 71, 8, 4210752);
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider) {
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(stack);
            if (!posList.isEmpty()) {
                BlockPos pos = posList.get(0);
                if (pos != null) {
                    float scale = 0.75F;
                    GlStateManager.pushMatrix();
                    GlStateManager.scaled(scale, scale, scale);
                    GlStateManager.translated(140 * (1 - scale), 28 * (1 - scale), 0);
                    font.drawString(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()), 105, 28, 0x404080);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(int par1, int par2, float par3) {
        super.render(par1, par2, par3);

        renderHoveredToolTip(par1, par2);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        renderBackground();
        minecraft.getTextureManager().bindTexture(Textures.GUI_INVENTORY_SEARCHER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(xStart, yStart, 0, 0, xSize, ySize);
    }
}
