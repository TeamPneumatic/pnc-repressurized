package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerItemSearcher;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
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
    private static final int SEARCH_SLOT = 48;

    private static List<SearchEntry> cachedSearchEntries;

    private final ItemStackHandler inventory = new ItemStackHandler(49);  // 6 * 8 slots, plus the selected item
    private final Screen parentScreen;
    // Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
    private double currentScroll;
    // True if the scrollbar is being dragged
    private boolean isScrolling;
    // True if the left mouse button was held down last time drawScreen was called.
    private TextFieldWidget searchField;
    private Rectangle2d scrollArea;
    private String lastSearch = "";
    private int updateCounter = 0;

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
        return inventory.getStackInSlot(SEARCH_SLOT);
    }

    public void setSearchStack(@Nonnull ItemStack stack) {
        inventory.setStackInSlot(SEARCH_SLOT, stack);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot != null) {
            if (slot.slotNumber == SEARCH_SLOT) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                inventory.setStackInSlot(SEARCH_SLOT, slot.getStack());
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

        searchField = new WidgetTextField(font, guiLeft + 8, guiTop + 36, 89, font.FONT_HEIGHT);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFF);
        searchField.setResponder(s -> textFieldResponder());
        addButton(searchField);
        setListener(searchField);
        searchField.setFocused2(true);

        scrollArea = new Rectangle2d(guiLeft + 156, guiTop + 48, 14, 112);

        updateCreativeSearch();
    }

    private void textFieldResponder() {
        if (!searchField.getText().equals(lastSearch)) {
            updateCounter = 5;
        }
        lastSearch = searchField.getText();
    }

    @Override
    public void closeScreen() {
        minecraft.keyboardListener.enableRepeatEvents(false);
        if (parentScreen != null) {
            ClientUtils.closeContainerGui(parentScreen);
        } else {
            super.closeScreen();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (updateCounter > 0 && --updateCounter == 0) {
            updateCreativeSearch();
        }

        if (parentScreen instanceof GuiArmorMainScreen
                && minecraft.player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
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
            list.add(EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchantment, i)));
        }
    }

    private Stream<SearchEntry> getSearchEntries() {
        if (cachedSearchEntries == null) {
            NonNullList<ItemStack> itemList = NonNullList.create();

            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                NonNullList<ItemStack> l = NonNullList.create();
                if (item != null && item.getGroup() != null) item.fillItemGroup(item.getGroup(), l);
                itemList.addAll(l);
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

    private boolean needsScrollBars() {
        return container.hasMoreThan1PageOfItemsInList();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        if (dir != 0 && needsScrollBars()) {
            int j = container.itemList.size() / 9 - 5 + 1;
            float i = dir > 0 ? 1f : -1f;
            currentScroll = MathHelper.clamp(currentScroll - i / j, 0.0, 1.0);
            container.scrollTo(currentScroll);
            return true;
        }
        return super.mouseScrolled(x, y, dir);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isScrolling = button == 0 && needsScrollBars() && scrollArea.contains((int)mouseX, (int)mouseY);
        if (isScrolling) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void scrollTo(double mouseY) {
        currentScroll = (mouseY - scrollArea.getY()) / scrollArea.getHeight();
        currentScroll = MathHelper.clamp(currentScroll, 0F, 1F);
        container.scrollTo(currentScroll);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isScrolling = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);

        super.render(matrixStack, x, y, partialTicks);

        renderHoveredTooltip(matrixStack, x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int par1, int par2) {
        font.drawString(matrixStack, I18n.format("pneumaticcraft.armor.upgrade.search"), 5, 5, 0x404040);
        font.drawString(matrixStack, I18n.format("pneumaticcraft.gui.progWidget.itemFilter.filterLabel"), 8, 25, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float par1, int par2, int par3) {
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(matrixStack, xStart, yStart, 0, 0, xSize, ySize);

        int x = scrollArea.getX();
        int y1 = scrollArea.getY();
        int y2 = y1 + scrollArea.getHeight();
        minecraft.getTextureManager().bindTexture(SCROLL_TEXTURE);
        blit(matrixStack, x, y1 + (int) ((y2 - y1 - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

    }

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
