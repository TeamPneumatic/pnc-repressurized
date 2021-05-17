package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIBlockHeatPropertiesCategory implements IRecipeCategory<HeatPropertiesRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable hotArea;
    private final IDrawable coldArea;
    private final IDrawable air;

    private static final Rectangle2d INPUT_AREA = new Rectangle2d(65, 44, 18, 18);
    private static final Rectangle2d COLD_AREA = new Rectangle2d(5, 44, 18, 18);
    private static final Rectangle2d HOT_AREA = new Rectangle2d(125, 44, 18, 18);

    public JEIBlockHeatPropertiesCategory() {
        this.localizedName = I18n.format("pneumaticcraft.gui.jei.title.heatProperties");
        this.background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 0, 0, 146, 73);
        this.icon = JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.JEI_THERMOMETER, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        this.hotArea = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 0, 31, 18);
        this.coldArea = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 18, 31, 18);
        this.air = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 36, 16, 16);
    }

    public static Collection<HeatPropertiesRecipe> getAllRecipes() {
        ImmutableList.Builder<HeatPropertiesRecipe> res = ImmutableList.builder();
        res.addAll(BlockHeatProperties.getInstance().getAllEntries(Minecraft.getInstance().world));
        return res.build();
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.HEAT_PROPERTIES;
    }

    @Override
    public Class<? extends HeatPropertiesRecipe> getRecipeClass() {
        return HeatPropertiesRecipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(HeatPropertiesRecipe recipe, IIngredients ingredients) {
        setInputIngredient(recipe.getBlock(), ingredients);

        setOutputIngredient(recipe.getTransformHot(), ingredients);
        setOutputIngredient(recipe.getTransformCold(), ingredients);
    }

    private void setInputIngredient(Block block, IIngredients ingredients) {
        if (block instanceof FlowingFluidBlock) {
            FluidStack stack = new FluidStack(((FlowingFluidBlock) block).getFluid(), 1000);
            ingredients.setInput(VanillaTypes.FLUID, stack);
        } else {
            ingredients.setInput(VanillaTypes.ITEM, new ItemStack(block));
        }
    }

    private void setOutputIngredient(BlockState state, IIngredients ingredients) {
        if (state != null) {
            if (state.getBlock() instanceof FlowingFluidBlock) {
                int level = state.hasProperty(FlowingFluidBlock.LEVEL) ? state.get(FlowingFluidBlock.LEVEL) : 0;
                FluidStack stack = new FluidStack(((FlowingFluidBlock) state.getBlock()).getFluid(), 1000 * level / 15);
                ingredients.setOutput(VanillaTypes.FLUID, stack);
            } else {
                ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(state.getBlock()));
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayout layout, HeatPropertiesRecipe recipe, IIngredients ingredients) {
        // no-op: we're not using slots, since we draw inputs/outputs with the block renderer
    }

    @Override
    public void draw(HeatPropertiesRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        int h = fontRenderer.FONT_HEIGHT;

        ITextComponent desc = recipe.getDescriptionKey().isEmpty() ?
                StringTextComponent.EMPTY :
                new StringTextComponent(" (" + I18n.format(recipe.getDescriptionKey()) + ")");
        fontRenderer.func_243248_b(matrixStack, recipe.getInputDisplayName().deepCopy().append(desc), 0, 0, 0x4040a0);

        ITextComponent temp = xlate("pneumaticcraft.waila.temperature").append(new StringTextComponent((recipe.getTemperature() - 273) + "°C"));
        fontRenderer.func_243248_b(matrixStack, temp, 0, h * 2, 0x404040);

        String res = NumberFormat.getNumberInstance(Locale.getDefault()).format(recipe.getThermalResistance());
        fontRenderer.drawString(matrixStack, I18n.format("pneumaticcraft.gui.jei.thermalResistance") + res, 0, h * 3, 0x404040);

        boolean showCapacity = false;
        if (recipe.getTransformCold() != null) {
            coldArea.draw(matrixStack, INPUT_AREA.getX() - coldArea.getWidth() - 5, 42);
            showCapacity = true;
        }
        if (recipe.getTransformHot() != null) {
            hotArea.draw(matrixStack, HOT_AREA.getX() - hotArea.getWidth() - 5, 42);
            showCapacity = true;
        }

        renderBlock(recipe.getBlockState(), matrixStack, INPUT_AREA.getX() + 9, INPUT_AREA.getY() + 1);
        renderBlock(recipe.getTransformCold(), matrixStack, COLD_AREA.getX() + 9, COLD_AREA.getY() + 1);
        renderBlock(recipe.getTransformHot(), matrixStack, HOT_AREA.getX() + 9, HOT_AREA.getY() + 1);

        if (showCapacity) {
            fontRenderer.drawString(matrixStack, I18n.format("pneumaticcraft.gui.jei.heatCapacity",
                    NumberFormat.getNumberInstance(Locale.getDefault()).format(recipe.getHeatCapacity())), 0, background.getHeight() - h, 0x404040);
        }
    }

    @Override
    public boolean handleClick(HeatPropertiesRecipe recipe, double mouseX, double mouseY, int mouseButton) {
        IFocus<?> focus = null;
        if (INPUT_AREA.contains((int)mouseX, (int)mouseY)) {
            focus = makeFocus(recipe.getBlock(), mouseButton == 0 ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT);
        } else if (recipe.getTransformCold() != null && COLD_AREA.contains((int)mouseX, (int)mouseY)) {
            focus = makeFocus(recipe.getTransformCold().getBlock(), mouseButton == 0 ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT);
        } else if (recipe.getTransformHot() != null && HOT_AREA.contains((int)mouseX, (int)mouseY)) {
            focus = makeFocus(recipe.getTransformHot().getBlock(), mouseButton == 0 ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT);
        }
        if (focus != null) {
            JEIPlugin.recipesGui.show(focus);
            return true;
        }
        return false;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(HeatPropertiesRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> l = new ArrayList<>();
        if (INPUT_AREA.contains((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getBlock(), l);
        } else if (recipe.getTransformCold() != null && COLD_AREA.contains((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getTransformCold().getBlock(), l);
        } else if (recipe.getTransformHot() != null && HOT_AREA.contains((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getTransformHot().getBlock(), l);
        } else if (mouseY > 20 && mouseY < 30) {
            l.add(xlate("pneumaticcraft.gui.jei.tooltip.thermalResistance"));
        } else if (recipe.getHeatCapacity() != 0 && mouseY > 62) {
            l.add(xlate("pneumaticcraft.gui.jei.tooltip.heatCapacity"));
        }
        return l;
    }

    private IFocus<?> makeFocus(Block block, IFocus.Mode mode) {
        if (block == Blocks.AIR) {
            return null;
        } else if (block instanceof FlowingFluidBlock) {
            FluidStack stack = new FluidStack(((FlowingFluidBlock) block).getFluid(), 1000);
            return JEIPlugin.recipeManager.createFocus(mode, stack);
        } else {
            ItemStack stack = new ItemStack(block);
            return JEIPlugin.recipeManager.createFocus(mode, stack);
        }
    }

    private void addTooltip(Block block, List<ITextComponent> list) {
        if (block instanceof FlowingFluidBlock) {
            FluidStack stack = new FluidStack(((FlowingFluidBlock) block).getFluid(), 1000);
            list.add(stack.getDisplayName());
            list.add(new StringTextComponent(ModNameCache.getModName(stack.getFluid())).mergeStyle(TextFormatting.BLUE, TextFormatting.ITALIC));
        } else {
            ItemStack stack = new ItemStack(block);
            list.add(stack.getDisplayName());
            stack.getItem().addInformation(stack, ClientUtils.getClientWorld(), list, ClientUtils.hasShiftDown() ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
            list.add(new StringTextComponent(ModNameCache.getModName(stack.getItem())).mergeStyle(TextFormatting.BLUE, TextFormatting.ITALIC));
        }
    }

    private void renderBlock(BlockState state, MatrixStack matrixStack, int x, int y) {
        if (state != null) {
            if (state.getBlock() instanceof FlowingFluidBlock) {
                int level = state.hasProperty(FlowingFluidBlock.LEVEL) ? state.get(FlowingFluidBlock.LEVEL) : 0;
                FluidStack stack = new FluidStack(((FlowingFluidBlock) state.getBlock()).getFluid(), 1000 * level / 15);
                GuiUtils.drawFluid(matrixStack, new Rectangle2d(x - 7, y - 2, 16, 16), stack, new FluidTank(1000));
            } else if (state.getBlock() == Blocks.AIR) {
                air.draw(matrixStack, x - 8, y - 2);
            } else {
                float rot = Minecraft.getInstance().world.getGameTime() % 360;
                GuiUtils.renderBlockInGui(matrixStack, state, x, y, 100, rot, 15f);
            }
        }
    }
}
