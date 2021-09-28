package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Custom JEI recipe click area which also shows the current recipe outputs if any (and the current recipe ID
 * if advanced tooltips are on - F3+H)
 */
public class CustomRecipeClickArea {
    static <T extends GuiPneumaticContainerBase<?,?>> void add(IGuiHandlerRegistration reg, Class<? extends T> guiContainerClass, int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
        reg.addGuiContainerHandler(guiContainerClass, new IGuiContainerHandler<T>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(T gui, double mouseX, double mouseY) {
                return Collections.singletonList(createClickableArea(gui, xPos, yPos, width, height, recipeCategoryUids));
            }
        });
    }

    private static <T extends GuiPneumaticContainerBase<?,?>> IGuiClickableArea createClickableArea(T gui, int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
        Rectangle2d area = new Rectangle2d(xPos, yPos, width, height);
        List<ResourceLocation> recipeCategoryUidList = ImmutableList.copyOf(recipeCategoryUids);
        return new IGuiClickableArea() {
            @Override
            public Rectangle2d getArea() {
                return area;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                recipesGui.showCategories(recipeCategoryUidList);
            }

            @Override
            public List<ITextComponent> getTooltipStrings() {
                Collection<ItemStack> items = gui.getTargetItems();
                Collection<FluidStack> fluids = gui.getTargetFluids();
                ImmutableList.Builder<ITextComponent> builder = ImmutableList.builder();
                if (!items.isEmpty() || !fluids.isEmpty()) {
                    builder.add(new StringTextComponent("Current Recipe:").withStyle(TextFormatting.GRAY));
                    for (ItemStack stack : items) {
                        if (!stack.isEmpty()) {
                            builder.add(new StringTextComponent(Symbols.ARROW_RIGHT + " ").append(stack.getHoverName())
                                    .withStyle(TextFormatting.YELLOW));
                        }
                    }
                    for (FluidStack stack : fluids) {
                        if (!stack.isEmpty()) {
                            builder.add(new StringTextComponent(Symbols.ARROW_RIGHT + " ").append(stack.getDisplayName())
                                    .withStyle(TextFormatting.AQUA));
                        }
                    }
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        builder.add(new StringTextComponent(gui.te.getCurrentRecipeIdSynced()).withStyle(TextFormatting.DARK_GRAY));
                    }
                    builder.add(StringTextComponent.EMPTY);
                }
                builder.add(new TranslationTextComponent("jei.tooltip.show.recipes"));
                return builder.build();
            }
        };
    }
}
