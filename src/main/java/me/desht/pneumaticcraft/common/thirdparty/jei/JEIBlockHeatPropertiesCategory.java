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
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIBlockHeatPropertiesCategory extends AbstractPNCCategory<HeatPropertiesRecipe> {
    private final IDrawable hotArea;
    private final IDrawable coldArea;
    private final IDrawable air;

    private static final ScreenRectangle INPUT_AREA = new ScreenRectangle(65, 44, 18, 18);
    private static final ScreenRectangle COLD_AREA = new ScreenRectangle(5, 44, 18, 18);
    private static final ScreenRectangle HOT_AREA = new ScreenRectangle(125, 44, 18, 18);
    private static final ScreenRectangle[] OUTPUT_AREAS = new ScreenRectangle[] { COLD_AREA, HOT_AREA };

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

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, HeatPropertiesRecipe recipe, IFocusGroup focuses) {
        builder.addInputHandler(new ClickHandler(INPUT_AREA, recipe, HeatPropertiesRecipe::getBlock));
        builder.addInputHandler(new ClickHandler(COLD_AREA, recipe, r -> r.getTransformCold().map(BlockBehaviour.BlockStateBase::getBlock).orElse(null)));
        builder.addInputHandler(new ClickHandler(HOT_AREA, recipe, r -> r.getTransformHot().map(BlockBehaviour.BlockStateBase::getBlock).orElse(null)));
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
        collectOutputs(recipe.getTransformCold().orElse(null), items, fluids);
        collectOutputs(recipe.getTransformHot().orElse(null), items, fluids);

        for (int idx = 0; idx < 2; idx++) {
            if (!fluids.get(idx).isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_AREAS[idx].position().x() + 2, OUTPUT_AREAS[idx].position().y() - 1)
                        .addIngredient(NeoForgeTypes.FLUID_STACK, fluids.get(idx));
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
                FluidStack stack = new FluidStack(l.fluid, FluidType.BUCKET_VOLUME * level / 15);
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
            FluidStack stack = new FluidStack(l.fluid, FluidType.BUCKET_VOLUME);
            builder.addSlot(RecipeIngredientRole.INPUT, INPUT_AREA.position().x() + 2, INPUT_AREA.position().y() - 1)
                            .addIngredient(NeoForgeTypes.FLUID_STACK, stack);
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

        recipe.getThermalResistance().ifPresent(resistance -> {
            String res = NumberFormat.getNumberInstance(Locale.getDefault()).format(resistance);
            graphics.drawString(fontRenderer, xlate("pneumaticcraft.gui.jei.thermalResistance").append(res), 0, h * 3, 0x404040, false);
        });

        boolean showCapacity = false;
        if (recipe.getTransformCold().isPresent()) {
            coldArea.draw(graphics, INPUT_AREA.position().x() - coldArea.getWidth() - 5, 42);
            showCapacity = true;
        }
        if (recipe.getTransformHot().isPresent()) {
            hotArea.draw(graphics, HOT_AREA.position().x() - hotArea.getWidth() - 5, 42);
            showCapacity = true;
        }

        renderBlock(recipe.getBlockState(), graphics, INPUT_AREA.position().x() + 9, INPUT_AREA.position().y() + 1);
        recipe.getTransformCold().ifPresent(state -> renderBlock(state, graphics, COLD_AREA.position().x() + 9, COLD_AREA.position().y() + 1));
        recipe.getTransformHot().ifPresent(state -> renderBlock(state, graphics, HOT_AREA.position().x() + 9, HOT_AREA.position().y() + 1));

        if (showCapacity) {
            recipe.getHeatCapacity().ifPresent(heatCapacity -> {
                graphics.drawString(fontRenderer, xlate("pneumaticcraft.gui.jei.heatCapacity",
                                NumberFormat.getNumberInstance(Locale.getDefault()).format(heatCapacity)),
                        0, getBackground().getHeight() - h, 0x404040, false
                );
            });
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, HeatPropertiesRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (INPUT_AREA.containsPoint((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getBlock(), tooltip);
        } else if (recipe.getTransformCold().isPresent() && COLD_AREA.containsPoint((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getTransformCold().get().getBlock(), tooltip);
        } else if (recipe.getTransformHot().isPresent() && HOT_AREA.containsPoint((int)mouseX, (int)mouseY)) {
            addTooltip(recipe.getTransformHot().get().getBlock(), tooltip);
        } else if (mouseY > 20 && mouseY < 30) {
            tooltip.add(xlate("pneumaticcraft.gui.jei.tooltip.thermalResistance"));
        } else if (recipe.getHeatCapacity().isPresent() && mouseY > 62) {
            tooltip.add(xlate("pneumaticcraft.gui.jei.tooltip.heatCapacity"));
        }
        super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
    }


    private void addTooltip(Block block, ITooltipBuilder tooltip) {
        ItemStack stack = new ItemStack(block);
        tooltip.add(stack.getHoverName());
        List<Component> list = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(ClientUtils.getClientLevel()), list, ClientUtils.hasShiftDown() ? Default.ADVANCED : Default.NORMAL);
        tooltip.addAll(list);
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            String regName = PneumaticCraftUtils.getRegistryName(stack.getItem()).map(ResourceLocation::toString).orElse("?");
            tooltip.add(Component.literal(regName).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal(ModNameCache.getModName(stack.getItem())).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
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

    public record ClickHandler(ScreenRectangle area, HeatPropertiesRecipe recipe, Function<HeatPropertiesRecipe, Block> blockGetter) implements IJeiInputHandler {
        @Override
        public ScreenRectangle getArea() {
            return area;
        }

        @Override
        public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
            if (input.getKey().getType() == InputConstants.Type.MOUSE) {
                int mouseButton = input.getKey().getValue();
                IFocus<?> focus = makeFocus(blockGetter.apply(recipe), mouseButton == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT);
                if (focus != null) {
                    if (!input.isSimulate()) {
                        JEIPlugin.recipesGui.show(focus);
                    }
                    return true;
                }
            }
            return false;
        }

        private static IFocus<?> makeFocus(Block block, RecipeIngredientRole mode) {
            return block == null || block == Blocks.AIR || block instanceof LiquidBlock ?
                    null :
                    JEIPlugin.jeiHelpers.getFocusFactory().createFocus(mode, VanillaTypes.ITEM_STACK, new ItemStack(block));
        }
    }
}
