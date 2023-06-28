/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIBlockHeatPropertiesCategory extends AbstractPNCCategory<HeatPropertiesRecipe> {
    private final IDrawable hotArea;
    private final IDrawable coldArea;
    private final IDrawable air;

    private static final Rect2i INPUT_AREA = new Rect2i(65, 44, 18, 18);
    private static final Rect2i COLD_AREA = new Rect2i(5, 44, 18, 18);
    private static final Rect2i HOT_AREA = new Rect2i(125, 44, 18, 18);
    private static final Rect2i[] OUTPUT_AREAS = new Rect2i[] { COLD_AREA, HOT_AREA };

    public JEIBlockHeatPropertiesCategory() {
        super(RecipeTypes.HEAT_PROPERTIES,
                xlate("pneumaticcraft.gui.jei.title.heatProperties"),
                guiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 0, 0, 146, 73),
                guiHelper()
                        .drawableBuilder(Textures.JEI_THERMOMETER, 0, 0, 16, 16)
                        .setTextureSize(16, 16)
                        .build()
        );
        this.hotArea = guiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 0, 31, 18);
        this.coldArea = guiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 18, 31, 18);
        this.air = guiHelper().createDrawable(Textures.GUI_JEI_HEAT_PROPERTIES, 150, 36, 16, 16);
    }

    public static List<HeatPropertiesRecipe> getAllRecipes() {
        // FIXME filtering out recipes whose input block has no item (e.g. minecraft:fire) is a kludge:
        //  it suppresses JEI errors when loading recipes, but such recipes still aren't shown in JEI
        //  (on the other hand the blocks in the recipe don't appear in JEI's display anyway so ¯\_(ツ)_/¯)
        return BlockHeatProperties.getInstance().getAllEntries(Minecraft.getInstance().level).stream()
                .filter(r -> r.getBlock() instanceof LiquidBlock || !new ItemStack(r.getBlock()).isEmpty())
                .sorted(Comparator.comparingInt(HeatPropertiesRecipe::getTemperature)
                        .thenComparing(o -> o.getInputDisplayName().getString()))
                .toList();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeatPropertiesRecipe recipe, IFocusGroup focuses) {
        setInputIngredient(builder, recipe);

        List<ItemStack> items = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        collectOutputs(recipe.getTransformCold(), items, fluids);
        collectOutputs(recipe.getTransformHot(), items, fluids);

        for (int idx = 0; idx < 2; idx++) {
            if (!fluids.get(idx).isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_AREAS[idx].getX() + 2, OUTPUT_AREAS[idx].getY() - 1)
                        .addIngredient(ForgeTypes.FLUID_STACK, fluids.get(idx));
            } else if (!items.get(idx).isEmpty()) {
                builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                        .addItemStack(items.get(idx));
            }
        }
    }

    private void collectOutputs(BlockState state, List<ItemStack> items, List<FluidStack> fluids) {
        if (state != null) {
            if (state.getBlock() instanceof LiquidBlock l) {
                int level = state.hasProperty(LiquidBlock.LEVEL) ? state.getValue(LiquidBlock.LEVEL) : 15;
                if (level == 0) level = 15;
                FluidStack stack = new FluidStack(l.getFluid(), 1000 * level / 15);
                fluids.add(stack);
                items.add(new ItemStack(Blocks.BARRIER));
            } else {
                ItemStack stack = new ItemStack(state.getBlock());
                items.add(stack.isEmpty() ? new ItemStack(Blocks.BARRIER) : stack);
                fluids.add(FluidStack.EMPTY);
            }
        } else {
            items.add(new ItemStack(Blocks.BARRIER));
            fluids.add(FluidStack.EMPTY);
        }
    }

    private void setInputIngredient(IRecipeLayoutBuilder builder, HeatPropertiesRecipe recipe) {
        Block block = recipe.getBlock();
        if (block instanceof LiquidBlock l) {
            FluidStack stack = new FluidStack(l.getFluid(), 1000);
            builder.addSlot(RecipeIngredientRole.INPUT, INPUT_AREA.getX() + 2, INPUT_AREA.getY() - 1)
                            .addIngredient(ForgeTypes.FLUID_STACK, stack);
        } else {
            // items are rendered as blocks by renderBlock()
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(block));
        }
    }

    @Override
    public void draw(HeatPropertiesRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;

        int h = fontRenderer.lineHeight;

        Component desc = recipe.getDescriptionKey().isEmpty() ?
                Component.empty() :
                Component.literal(" (" + I18n.get(recipe.getDescriptionKey()) + ")");
        graphics.drawString(fontRenderer, recipe.getInputDisplayName().copy().append(desc), 0, 0, 0x4040a0, false);

        Component temp = xlate("pneumaticcraft.waila.temperature").append(Component.literal((recipe.getTemperature() - 273) + "°C"));
        graphics.drawString(fontRenderer, temp, 0, h * 2, 0x404040, false);

        String res = NumberFormat.getNumberInstance(Locale.getDefault()).format(recipe.getThermalResistance());
        graphics.drawString(fontRenderer, xlate("pneumaticcraft.gui.jei.thermalResistance").append(res), 0, h * 3, 0x404040, false);

        boolean showCapacity = false;
        if (recipe.getTransformCold() != null) {
            coldArea.draw(graphics, INPUT_AREA.getX() - coldArea.getWidth() - 5, 42);
            showCapacity = true;
        }
        if (recipe.getTransformHot() != null) {
            hotArea.draw(graphics, HOT_AREA.getX() - hotArea.getWidth() - 5, 42);
            showCapacity = true;
        }

        renderBlock(recipe.getBlockState(), graphics, INPUT_AREA.getX() + 9, INPUT_AREA.getY() + 1);
        renderBlock(recipe.getTransformCold(), graphics, COLD_AREA.getX() + 9, COLD_AREA.getY() + 1);
        renderBlock(recipe.getTransformHot(), graphics, HOT_AREA.getX() + 9, HOT_AREA.getY() + 1);

        if (showCapacity) {
            graphics.drawString(fontRenderer, xlate("pneumaticcraft.gui.jei.heatCapacity",
                    NumberFormat.getNumberInstance(Locale.getDefault()).format(recipe.getHeatCapacity())),
                    0, getBackground().getHeight() - h, 0x404040, false
            );
        }
    }

    @Override
    public boolean handleInput(HeatPropertiesRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE) {
            int mouseButton = input.getValue();
            IFocus<?> focus = null;
            if (INPUT_AREA.contains((int) mouseX, (int) mouseY)) {
                focus = makeFocus(recipe.getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
            } else if (recipe.getTransformCold() != null && COLD_AREA.contains((int) mouseX, (int) mouseY)) {
                focus = makeFocus(recipe.getTransformCold().getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
            } else if (recipe.getTransformHot() != null && HOT_AREA.contains((int) mouseX, (int) mouseY)) {
                focus = makeFocus(recipe.getTransformHot().getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
            }
            if (focus != null) {
                JEIPlugin.recipesGui.show(focus);
                return true;
            }
        }
        return false;
    }

//    @Override
//    public boolean handleClick(HeatPropertiesRecipe recipe, double mouseX, double mouseY, int mouseButton) {
//        IFocus<?> focus = null;
//        if (INPUT_AREA.contains((int)mouseX, (int)mouseY)) {
//            focus = makeFocus(recipe.getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
//        } else if (recipe.getTransformCold() != null && COLD_AREA.contains((int)mouseX, (int)mouseY)) {
//            focus = makeFocus(recipe.getTransformCold().getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
//        } else if (recipe.getTransformHot() != null && HOT_AREA.contains((int)mouseX, (int)mouseY)) {
//            focus = makeFocus(recipe.getTransformHot().getBlock(), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
//        }
//        if (focus != null) {
//            JEIPlugin.recipesGui.show(focus);
//            return true;
//        }
//        return false;
//    }

    @Override
    public List<Component> getTooltipStrings(HeatPropertiesRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> l = new ArrayList<>();
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

    private IFocus<?> makeFocus(Block block, RecipeIngredientRole mode) {
        return block == Blocks.AIR || block instanceof LiquidBlock ?
                null :
                JEIPlugin.jeiHelpers.getFocusFactory().createFocus(mode, VanillaTypes.ITEM_STACK, new ItemStack(block));
    }

    private void addTooltip(Block block, List<Component> list) {
        ItemStack stack = new ItemStack(block);
        list.add(stack.getHoverName());
        stack.getItem().appendHoverText(stack, ClientUtils.getClientLevel(), list, ClientUtils.hasShiftDown() ? Default.ADVANCED : Default.NORMAL);
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            String regName = PneumaticCraftUtils.getRegistryName(stack.getItem()).map(ResourceLocation::toString).orElse("?");
            list.add(Component.literal(regName).withStyle(ChatFormatting.DARK_GRAY));
        }
        list.add(Component.literal(ModNameCache.getModName(stack.getItem())).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
    }

    private void renderBlock(BlockState state, GuiGraphics graphics, int x, int y) {
        // note: fluid rendering is done by JEI (fluidstacks are registered in the recipe layout)
        if (state != null) {
            if (state.getBlock() == Blocks.AIR) {
                air.draw(graphics, x - 8, y - 2);
            } else {
                float rot = ClientUtils.getClientLevel().getGameTime() % 360;
                GuiUtils.renderBlockInGui(graphics, state, x, y, 100, rot, 15f);
            }
        }
    }
}
