package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerPlasticMixer;
import me.desht.pneumaticcraft.common.recipes.PlasticMixerRegistry;
import me.desht.pneumaticcraft.common.recipes.PlasticMixerRegistry.PlasticMixerRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPlasticMixer extends GuiPneumaticContainerBase<TileEntityPlasticMixer> {
    private GuiButtonSpecial[] buttons;
    private GuiCheckBox lockSelection;
    private WidgetLabel noItemsLabel;
    private WidgetLabel amountLabel;
    private WidgetTemperature tempWidget;
    private int nExposedFaces;
    private GuiAnimatedStat selectionTab;
    private Fluid lastFluid;
    private ItemStack lastItemStack = ItemStack.EMPTY;

    public GuiPlasticMixer(PlayerInventory player, TileEntityPlasticMixer te) {
        super(new ContainerPlasticMixer(player, te), te, Textures.GUI_PLASTIC_MIXER);
    }

    @Override
    public void initGui() {
        super.initGui();

        addWidget(new WidgetTemperature(0, guiLeft + 55, guiTop + 25, 273, 773, te.getLogic(0)));
        addWidget(tempWidget = new WidgetTemperature(1, guiLeft + 82, guiTop + 25, 273, 773, te.getLogic(1), 273) {
            @Override
            public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);
                if (getScales().length >= 2) {
                    TextFormatting tf = getScales()[1] <= te.getLogic(1).getTemperatureAsInt() ? TextFormatting.GREEN : TextFormatting.GOLD;
                    curTip.add(tf + "Required Temperature: " + (getScales()[1] - 273) + "\u00b0C");
                }
            }
        });
        addWidget(new WidgetTank(guiLeft + 152, guiTop + 14, te.getTank()));

        PlasticMixerRecipe recipe = PlasticMixerRegistry.INSTANCE.getRecipe(te.getTank().getFluid());
        Item targetItem = recipe == null ? ModItems.PLASTIC : recipe.getItemStack().getItem();
        selectionTab = addAnimatedStat("gui.tab.plasticMixer.plasticSelection", new ItemStack(targetItem, 1, 1), 0xFF005500, false);
        selectionTab.addPadding(12, 88 / fontRenderer.getStringWidth(" "));

        buttons = new GuiButtonSpecial[16];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int index = y * 4 + x;
                ItemStack stack = new ItemStack(targetItem, 1, index);
                buttons[index] = new GuiButtonSpecial(index + 1, x * 21 + 15, y * 21 + 30, 20, 20, "")
                        .setRenderStacks(stack)
                        .setTooltipText(stack.getDisplayName());
                selectionTab.addSubWidget(buttons[index]);
            }
        }
        noItemsLabel = new WidgetLabel(15, 34, TextFormatting.GOLD.toString() + TextFormatting.ITALIC + I18n.format("gui.tab.plasticMixer.tankEmpty"));
        selectionTab.addSubWidget(noItemsLabel);
        amountLabel = new WidgetLabel(15, 118, "");
        selectionTab.addSubWidget(amountLabel);

        selectionTab.addSubWidget(lockSelection = new GuiCheckBox(17, 15, 18, 0xFF000000, "gui.plasticMixer.lockSelection")
                .setChecked(te.lockSelection)
                .setTooltip(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.plasticMixer.lockSelection.tooltip"))));

        if (te.getTank().getFluid() != null && te.getTank().getFluid().amount > 0) {
            selectionTab.openWindow();
        }
        updateSelectionTab();

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    private void updateSelectionTab() {
        PlasticMixerRecipe recipe = PlasticMixerRegistry.INSTANCE.getRecipe(te.getTank().getFluid());
        boolean recipeOK = recipe != null && recipe.allowSolidifying();

        int nTypes = recipe == null ? 0 : recipe.getNumSubTypes();
        for (int index = 0; index < 16; index++) {
            if (recipeOK) {
                ItemStack stack = new ItemStack(recipe.getItemStack().getItem(), 1, index);
                buttons[index].setRenderStacks(stack).setTooltipText(stack.getDisplayName());
            }
            buttons[index].setVisible(recipeOK && index < nTypes);
            buttons[index].visible = recipeOK && index < nTypes;
        }
        if (recipeOK) {
            FluidStack f = recipe.getFluidStack();
            amountLabel.text = StringUtils.abbreviate(TextFormatting.GRAY + "" + f.amount + "mB " + f.getFluid().getLocalizedName(f), 20);
        }
        amountLabel.visible = recipeOK;
        noItemsLabel.visible = !recipeOK;
        selectionTab.setTexture(recipeOK ? recipe.getItemStack() : new ItemStack(Blocks.STRUCTURE_VOID));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (selectionTab != null) {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].enabled = te.selectedPlastic != i;
            }
            lockSelection.checked = te.lockSelection;
        }

        Fluid f = getFluid();
        if (f != lastFluid) {
            updateSelectionTab();
        }
        lastFluid = getFluid();

        ItemStack input = te.getInventoryCap().getStackInSlot(TileEntityPlasticMixer.INV_INPUT);
        if (!ItemStack.areItemsEqual(input, lastItemStack)) {
            PlasticMixerRecipe recipe = PlasticMixerRegistry.INSTANCE.getRecipe(input);
            if (recipe != null && recipe.allowMelting()) {
                tempWidget.setScales(273, recipe.getTemperature());
            } else {
                tempWidget.setScales(273);
            }
        }
        lastItemStack = te.getInventoryCap().getStackInSlot(TileEntityPlasticMixer.INV_OUTPUT).copy();
    }

    private Fluid getFluid() {
        return te.getTank().getFluid() == null ? null : te.getTank().getFluid().getFluid();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString("Upgr.", 15, 19, 4210752);
        fontRenderer.drawString("Hull", 56, 16, 4210752);
        fontRenderer.drawString("Item", 88, 16, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        for (int i = 0; i < 3; i++) {
            double percentage = (double) te.dyeBuffers[i] / TileEntityPlasticMixer.DYE_BUFFER_MAX;
            drawVerticalLine(guiLeft + 124, guiTop + 35 + i * 18, guiTop + 37 - MathHelper.clamp((int) (percentage * 16), 1, 15) + i * 18, 0xFF000000 | 0xFF0000 >> 8 * i);
        }
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -1);
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        ItemStack stack = te.getInventoryCap().getStackInSlot(0);
        if (te.getTank().getFluidAmount() == 0) {
            if (stack.isEmpty()) {
                curInfo.add("gui.tab.problems.plasticMixer.noPlastic");
            } else {
                curInfo.add("gui.tab.problems.notEnoughHeat");
            }
        } else if (!stack.isEmpty()) {
            PlasticMixerRecipe recipe = PlasticMixerRegistry.INSTANCE.getRecipe(stack);
            int temp = recipe == null ? PneumaticValues.PLASTIC_MIXER_MELTING_TEMP : recipe.getTemperature();
            int amount = recipe == null ? 1000 : recipe.getFluidStack().amount;
            if (te.getLogic(1).getTemperatureAsInt() >= temp && te.getTank().getCapacity() - te.getTank().getFluidAmount() < amount) {
                curInfo.add("gui.tab.problems.plasticMixer.plasticOverflow");
            }
        }
        if (te.getInventoryCap().getStackInSlot(TileEntityPlasticMixer.INV_DYE_RED).isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 1).getDisplayName()));
        }
        if (te.getInventoryCap().getStackInSlot(TileEntityPlasticMixer.INV_DYE_GREEN).isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 2).getDisplayName()));
        }
        if (te.getInventoryCap().getStackInSlot(TileEntityPlasticMixer.INV_DYE_BLUE).isEmpty()  ) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 4).getDisplayName()));
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        if (curInfo.size() == 0) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noProblems"));
        }
    }


    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (nExposedFaces > 0 && !te.getInventoryCap().getStackInSlot(0).isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.exposedFaces", nExposedFaces, 6));
        }
    }
}
