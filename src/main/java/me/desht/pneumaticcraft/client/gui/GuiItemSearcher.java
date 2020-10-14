package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerItemSearcher;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiItemSearcher extends ContainerScreen<ContainerItemSearcher> {
    private static final ResourceLocation GUI_TEXTURE = Textures.GUI_ITEM_SEARCHER;
    private static final ResourceLocation SCROLL_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static List<SearchEntry> cachedSearchEntries;
    private final ItemStackHandler inventory = new ItemStackHandler(49);
    private final Screen parentScreen;
    // Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
    private float currentScroll;
    // True if the scrollbar is being dragged
    private boolean isScrolling;
    // True if the left mouse button was held down last time drawScreen was called.
    private boolean wasClicking;
    private TextFieldWidget searchField;
    private boolean firstRun = true;

    public GuiItemSearcher(ContainerItemSearcher container, PlayerInventory playerInventory, ITextComponent displayString) {
        super(container, playerInventory, displayString);

        passEvents = true;
        ySize = 176;
        parentScreen = Minecraft.getInstance().currentScreen;
        container.init(this);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Nonnull
    public ItemStack getSearchStack() {
        return inventory.getStackInSlot(48);
    }

    public void setSearchStack(@Nonnull ItemStack stack) {
        inventory.setStackInSlot(48, stack);
    }

    @Override
    protected void handleMouseClick(Slot par1Slot, int par2, int par3, ClickType par4) {
        if (par1Slot != null) {
            if (par1Slot.slotNumber == 48) {
                par1Slot.putStack(ItemStack.EMPTY);
            } else {
                inventory.setStackInSlot(48, par1Slot.getStack());
            }
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void init() {
        super.init();

        buttons.clear();
        children.clear();
        minecraft.keyboardListener.enableRepeatEvents(true);
        searchField = new TextFieldWidget(font, guiLeft + 20, guiTop + 36, 89, font.FONT_HEIGHT, StringTextComponent.EMPTY);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setResponder(s -> updateCreativeSearch());
        addButton(searchField);
        setListener(searchField);
        searchField.setFocused2(true);

        updateCreativeSearch();
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

    @Override
    public void tick() {
        super.tick();

        if (parentScreen instanceof GuiArmorMainScreen
                && minecraft.player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
//                minecraft.displayGuiScreen(parentScreen);
//                onClose();
            closeScreen();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeScreen();
        }
        return !searchField.keyPressed(keyCode, scanCode, modifiers)
                && searchField.canWrite() || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void getAllEnchantedBooks(Enchantment enchantment, NonNullList<ItemStack> list) {
        for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
            ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
            itemstack.addEnchantment(enchantment, i);
            list.add(itemstack);
        }
    }

    private Stream<SearchEntry> getSearchEntries() {
        if (cachedSearchEntries == null) {
            NonNullList<ItemStack> itemList = NonNullList.create();

            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item != null) itemList.add(new ItemStack(item));
            }

            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
                if (enchantment != null && enchantment.type != null) {
                    getAllEnchantedBooks(enchantment, itemList);
                }
            }

            cachedSearchEntries = itemList.stream().map(SearchEntry::new).collect(Collectors.toList());
        }
        return cachedSearchEntries.stream();
    }

    private void updateCreativeSearch() {
        container.itemList.clear();

        String s = searchField.getText().toLowerCase();

        List<ItemStack> applicableEntries = getSearchEntries()
                                                .filter(entry -> entry.test(s))
                                                .map(entry -> entry.stack)
                                                .collect(Collectors.toList());

        container.itemList.addAll(applicableEntries);

        currentScroll = 0.0F;
        container.scrollTo(0.0F);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int par1, int par2) {
        font.drawString(matrixStack, "Item Searcher", 23, 5, 0x404040);
        font.drawString(matrixStack, "Search Box", 23, 25, 0x404040);
        font.drawString(matrixStack, "Target", 113, 10, 0x404040);
    }

    /**
     * returns (if you are not on the inventoryTab) and (the flag isn't set) and( you have more than 1 page of items)
     */
    private boolean needsScrollBars() {
        return container.hasMoreThan1PageOfItemsInList();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        if (dir != 0 && needsScrollBars()) {
            int j = container.itemList.size() / 9 - 5 + 1;
            float i = dir > 0 ? 1f : -1f;
            currentScroll = MathHelper.clamp(currentScroll - i /  j, 0F, 1F);
            container.scrollTo(currentScroll);
            return true;
        }
        return super.mouseScrolled(x, y, dir);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);

        boolean isLeftClicking = minecraft.gameSettings.keyBindAttack.isKeyDown();
        int x1 = guiLeft + 156;
        int y1 = guiTop + 48;
        int x2 = x1 + 14;
        int y2 = y1 + 112;

        if (!wasClicking && isLeftClicking && x >= x1 && y >= y1 && x < x2 && y < y2) {
            isScrolling = needsScrollBars();
        }

        if (!isLeftClicking) {
            isScrolling = false;
        }

        wasClicking = isLeftClicking;

        if (isScrolling) {
            currentScroll = (y - y1 - 7.5F) / (y2 - y1 - 15.0F);
            currentScroll = MathHelper.clamp(currentScroll, 0F, 1F);
            container.scrollTo(currentScroll);
        }
        if (firstRun) {
            firstRun = false;
            container.scrollTo(0);
        }

        super.render(matrixStack, x, y, partialTicks);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableLighting();

        renderHoveredTooltip(matrixStack, x, y);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float par1, int par2, int par3) {
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(matrixStack, xStart, yStart, 0, 0, xSize, ySize);

        int i1 = guiLeft + 156;
        int k = guiTop + 48;
        int l = k + 112;
        minecraft.getTextureManager().bindTexture(SCROLL_TEXTURE);
        blit(matrixStack, i1, k + (int) ((l - k - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

    }

    /**
     * Returns the creative inventory
     */
    public IItemHandlerModifiable getInventory() {
        return inventory;
    }
    
    public class SearchEntry implements Predicate<String> {
        public final ItemStack stack;
        private final String tooltip;
        
        SearchEntry(ItemStack stack) {
            this.stack = stack;

            List<String> l;
            try {
                l = stack.getTooltip(minecraft.player, minecraft.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL).stream()
                        .map(ITextComponent::getString)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {
                // it's possible some modded item could have a buggy addInformation() implementation
                l = Collections.emptyList();
            }
            tooltip = StringUtils.join(l, "\n").toLowerCase();
        }
        
        @Override
        public boolean test(String searchString){
            return tooltip.contains(searchString);
        }
    }
}
