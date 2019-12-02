package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron.EnumProblemState;
import me.desht.pneumaticcraft.common.inventory.SlotUntouchable;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronInvSync;
import me.desht.pneumaticcraft.common.network.PacketAmadronOrderUpdate;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiAmadron extends GuiPneumaticContainerBase<ContainerAmadron,TileEntityBase> {
    private WidgetTextField searchBar;
    private WidgetVerticalScrollbar scrollbar;
    private int page;
    private final List<WidgetAmadronOffer> widgetOffers = new ArrayList<>();
    private boolean needsRefreshing;
    private boolean hadProblem = false;
    private GuiButtonSpecial orderButton;
    private GuiButtonSpecial addTradeButton;

    public GuiAmadron(ContainerAmadron container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        xSize = 176;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        String amadron = I18n.format("gui.amadron.title");
        addLabel(amadron, guiLeft + xSize / 2 - font.getStringWidth(amadron) / 2, guiTop + 5, 0xFFFFFF);
        addLabel(I18n.format("gui.search"), guiLeft + 76 - font.getStringWidth(I18n.format("gui.search")), guiTop + 41, 0xFFFFFF);

        addInfoTab(I18n.format("gui.tooltip.item.amadron_tablet"));
        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.HOPPER), 0xFF00AAFF, true)
                .setText("gui.tab.info.ghostSlotInteraction");
        addAnimatedStat("gui.tab.amadron.disclaimer.title", new ItemStack(Items.WRITABLE_BOOK), 0xFF0000FF, true)
                .setText("gui.tab.amadron.disclaimer");
        GuiAnimatedStat customTrades = addAnimatedStat("gui.tab.amadron.customTrades", new ItemStack(Items.DIAMOND), 0xFFD07000, false);
        customTrades.addPadding(3, 21);
        searchBar = new WidgetTextField(font, guiLeft + 79, guiTop + 40, 73, font.FONT_HEIGHT);
        searchBar.setFocused2(true);
        searchBar.setResponder(s -> {
            needsRefreshing = true;
            scrollbar.setCurrentState(0);
        });
        addButton(searchBar);

        scrollbar = new WidgetVerticalScrollbar(guiLeft + 156, guiTop + 54, 142);
        scrollbar.setStates(1);
        scrollbar.setListening(true);
        addButton(scrollbar);

        List<String> tooltip = PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.order.tooltip"), 40);
        orderButton = new GuiButtonSpecial(guiLeft + 52, guiTop + 16, 72, 20, I18n.format("gui.amadron.button.order")).setTooltipText(tooltip).withTag("order");
        addButton(orderButton);

        addTradeButton = new GuiButtonSpecial(16, 16, 20, 20, "")
                .setRenderStacks(new ItemStack(Items.GOLD_INGOT)).withTag("addPlayerTrade");
        customTrades.addSubWidget(addTradeButton);
        int startX = 40;
        if (ContainerAmadron.mayAddPeriodicOffers) {
            GuiButtonSpecial addPeriodicButton = new GuiButtonSpecial(startX, 16, 20, 20, "")
                    .setRenderStacks(new ItemStack(Items.CLOCK)).setTooltipText(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.addPeriodicTrade"), 40)).withTag("addPeriodicTrade");
            customTrades.addSubWidget(addPeriodicButton);
            startX += 24;
        }
        if (ContainerAmadron.mayAddStaticOffers) {
            GuiButtonSpecial addStaticButton = new GuiButtonSpecial(startX, 16, 20, 20, "")
                    .setRenderStacks(new ItemStack(Items.EMERALD)).setTooltipText(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.addStaticTrade"), 40)).withTag("addStaticTrade");
            customTrades.addSubWidget(addStaticButton);
        }

        needsRefreshing = true;
    }

    @Override
    protected int getBackgroundTint() {
        return 0x068e2c;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AMADRON;
    }

    @Override
    public void tick() {
        super.tick();

        if (needsRefreshing || page != scrollbar.getState()) {
            setPage(scrollbar.getState());
        }
        for (WidgetAmadronOffer offer : widgetOffers) {
            offer.setCanBuy(container.buyableOffers[container.offers.indexOf(offer.getOffer())]);
            offer.setShoppingAmount(container.getShoppingCartAmount(offer.getOffer()));
        }
        if (!hadProblem && container.problemState != EnumProblemState.NO_PROBLEMS) {
            problemTab.openWindow();
        }
        hadProblem = container.problemState != EnumProblemState.NO_PROBLEMS;
        orderButton.active = !container.isBasketEmpty();
        addTradeButton.active = container.currentOffers < container.maxOffers;
        List<String> tooltip = new ArrayList<>();
        tooltip.add(I18n.format("gui.amadron.button.addTrade"));
        tooltip.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.addTrade.tooltip"), 40));
        tooltip.add((addTradeButton.active ? TextFormatting.GRAY : TextFormatting.RED) + I18n.format("gui.amadron.button.addTrade.tooltip.offerCount", container.currentOffers, container.maxOffers == Integer.MAX_VALUE ? "\u221E" : container.maxOffers));
        addTradeButton.setTooltipText(tooltip);
    }

    public void setPage(int page) {
        this.page = page;
        updateVisibleOffers();
    }

    private void updateVisibleOffers() {
        needsRefreshing = false;
        int invSize = ContainerAmadron.ROWS * 2;
        container.clearStacks();
        List<AmadronOffer> offers = container.offers;
        List<AmadronOffer> visibleOffers = new ArrayList<>();
        int skippedOffers = 0;
        int applicableOffers = 0;
        for (AmadronOffer offer : offers) {
            if (offer.passesQuery(searchBar.getText())) {
                applicableOffers++;
                if (skippedOffers < page * invSize) {
                    skippedOffers++;
                } else if (visibleOffers.size() < invSize) {
                    visibleOffers.add(offer);
                }
            }
        }

        scrollbar.setStates(Math.max(1, (applicableOffers + invSize - 1) / invSize - 1));

        buttons.removeAll(widgetOffers);
        children.removeAll(widgetOffers);
        for (int i = 0; i < visibleOffers.size(); i++) {
            AmadronOffer offer = visibleOffers.get(i);
            if (!offer.getInput().getItem().isEmpty()) {
                container.getSlot(i * 2).putStack(offer.getInput().getItem());
                ((SlotUntouchable) container.getSlot(i * 2)).setEnabled(true);
            }
            if (!offer.getOutput().getItem().isEmpty()) {
                container.getSlot(i * 2 + 1).putStack(offer.getOutput().getItem());
                ((SlotUntouchable) container.getSlot(i * 2 + 1)).setEnabled(true);
            }

            WidgetAmadronOffer widget = new WidgetAmadronOffer(guiLeft + 6 + 73 * (i % 2), guiTop + 55 + 35 * (i / 2), offer) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
                    NetworkHandler.sendToServer(new PacketAmadronOrderUpdate(container.offers.indexOf(getOffer()), mouseButton, PneumaticCraftRepressurized.proxy.isSneakingInGui()));
                    return true;
                }
            };
            addButton(widget);
            widgetOffers.add(widget);
        }

        // avoid drawing phantom slot highlights where there's no widget
        for (int i = visibleOffers.size() * 2; i < container.inventorySlots.size(); i++) {
            ((SlotUntouchable) container.getSlot(i)).setEnabled(false);
        }

        // the server also needs to know what's in the tablet, or the next
        // "window items" packet will empty all the client-side slots
        NetworkHandler.sendToServer(new PacketAmadronInvSync(container.getInventory()));
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (container.problemState != EnumProblemState.NO_PROBLEMS) {
            curInfo.add(container.problemState.getTranslationKey());
        }
    }
}
