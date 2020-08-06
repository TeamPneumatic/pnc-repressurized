package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.common.inventory.ContainerJackhammerSetup;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemDrillBit.DrillBitType;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiJackHammerSetup extends GuiPneumaticContainerBase<ContainerJackhammerSetup, TileEntityBase> {
    private final WidgetButtonExtended[] typeButtons = new WidgetButtonExtended[DigMode.values().length];
    private WidgetButtonExtended selectorButton;

    public GuiJackHammerSetup(ContainerJackhammerSetup container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 182;
    }

    @Override
    public void init() {
        super.init();

        ItemStack hammerStack = playerInventory.player.getHeldItem(container.getHand());

        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);

        if (digMode != null) {
            addButton(selectorButton = new WidgetButtonExtended(guiLeft + 127, guiTop + 67, 20, 20,
                    "", b -> toggleShowChoices()))
                    .setRenderedIcon(digMode.getGuiIcon());

            int xBase = 147 - 20 * typeButtons.length;
            for (DigMode dm : DigMode.values()) {
                typeButtons[dm.ordinal()] = new WidgetButtonExtended(guiLeft + xBase + 20 * dm.ordinal(), guiTop + 47, 20, 20,
                        "", b -> selectDigMode(dm))
                        .setRenderedIcon(dm.getGuiIcon())
                        .withTag("digmode:" + dm.toString());
                typeButtons[dm.ordinal()].visible = false;
                addButton(typeButtons[dm.ordinal()]);
            }
        }

        addButton(new WidgetTooltipArea(guiLeft + 96, guiTop + 19, 18, 18) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shiftPressed) {
                if (!container.inventorySlots.get(1).getHasStack()) {
                    curTip.add(xlate("pneumaticcraft.gui.tooltip.jackhammer.enchantedBookTip"));
                }
            }
        });
    }

    private void selectDigMode(DigMode digMode) {
        // communication to server handled via PacketGuiButton
        Arrays.stream(typeButtons).forEach(button -> button.visible = false);
        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    private void toggleShowChoices() {
        Arrays.stream(typeButtons).forEach(button -> button.visible = !button.visible);
    }

    @Override
    public void tick() {
        super.tick();

        updateDigModeButtons();
    }

    private void updateDigModeButtons() {
        ItemStack drillStack = container.getSlot(0).getStack();
        DrillBitType bitType = drillStack.getItem() instanceof ItemDrillBit ?
                ((ItemDrillBit) drillStack.getItem()).getType() :
                DrillBitType.NONE;

        ItemStack hammerStack = playerInventory.player.getHeldItem(container.getHand());
        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);
        if (digMode == null) digMode = DigMode.MODE_1X1;

        for (DigMode dm : DigMode.values()) {
            typeButtons[dm.ordinal()].active = dm.getBitType().getTier() <= bitType.getTier();
        }

        if (digMode.getBitType().getTier() > bitType.getTier() && digMode != DigMode.MODE_1X1) {
            // jackhammer currently has a selected dig type of a tier too high for the installed drill bit
            digMode = DigMode.MODE_1X1;
            NetworkHandler.sendToServer(new PacketGuiButton("type:" + digMode.toString()));
        }

        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (!container.inventorySlots.get(0).getHasStack()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.jackhammer.noBit"));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_JACKHAMMER_SETUP;
    }
}
