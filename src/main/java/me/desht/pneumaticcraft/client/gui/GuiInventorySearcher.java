package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.inventory.ContainerInventorySearcher;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class GuiInventorySearcher extends GuiContainer {
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private final GuiScreen parentScreen;
    private Predicate<ItemStack> stackPredicate = itemStack -> true;

    public GuiInventorySearcher(EntityPlayer par1EntityPlayer) {
        super(new ContainerInventorySearcher(par1EntityPlayer.inventory));
        par1EntityPlayer.openContainer = inventorySlots;
        allowUserInput = true;
        ySize = 176; //TODO change
        parentScreen = FMLClientHandler.instance().getClient().currentScreen;
        ((ContainerInventorySearcher) inventorySlots).init(inventory);
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
            stack = stack.copy();
            stack.setCount(1);
            inventory.setStackInSlot(0, stack);
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

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (par2 == 1)//esc
        {
            mc.displayGuiScreen(parentScreen);
            onGuiClosed();
        } else {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        fontRenderer.drawString("Inventory", 7, 5, 4210752);
        fontRenderer.drawString("Searcher", 7, 15, 4210752);
        fontRenderer.drawString("Target", 71, 8, 4210752);
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider) {
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(stack);
            BlockPos pos = posList.get(0);
            if (pos != null) {
                float scale = 0.75F;
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.translate(140 * (1 - scale), 28 * (1 - scale), 0);
                fontRenderer.drawString(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()), 105, 28, 0x404080);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        renderHoveredToolTip(par1, par2);
        /*
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disable();*/
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(Textures.GUI_INVENTORY_SEARCHER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
    }
}
