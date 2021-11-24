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

package me.desht.pneumaticcraft.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.IGuiDrone;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getPlayer() == null) return;

        ItemStack stack = event.getItemStack();


        if (stack.getItem() instanceof BucketItem) {
            handleFluidContainerTooltip(event);
        } else if (stack.getItem().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
            addStandardTooltip(stack, event.getToolTip(), event.getFlags());
        }
        if (stack.getItem() instanceof IProgrammable) {
            handleProgrammableTooltip(event);
        }
    }

    private static void addStandardTooltip(ItemStack stack, List<ITextComponent> curInfo, ITooltipFlag flagIn) {
        addPressureTooltip(stack, curInfo);

        if (stack.getItem() instanceof IUpgradeAcceptor) {
            UpgradableItemUtils.addUpgradeInformation(stack, curInfo, ITooltipFlag.TooltipFlags.NORMAL);
        }

        if (stack.getItem() instanceof IInventoryItem) {
            List<ItemStack> stacks = new ArrayList<>();
            IInventoryItem item = (IInventoryItem) stack.getItem();
            item.getStacksInItem(stack, stacks);
            if (item.getInventoryHeader() != null && !stacks.isEmpty()) {
                curInfo.add(item.getInventoryHeader());
            }
            PneumaticCraftUtils.summariseItemStacks(curInfo, stacks.toArray(new ItemStack[0]), item.getTooltipPrefix(stack));
        }

        String key = ICustomTooltipName.getTranslationKey(stack, true);
        if (I18n.exists(key)) {
            if (ClientUtils.hasShiftDown()) {
                String translatedInfo = TextFormatting.AQUA + I18n.get(key);
                curInfo.addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.splitString(translatedInfo)));
                if (!ThirdPartyManager.instance().getDocsProvider().isInstalled()) {
                    curInfo.add(xlate("pneumaticcraft.gui.tab.info.installDocsProvider"));
                }
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.sneakForInfo").withStyle(TextFormatting.AQUA));
            }
        }
    }

    private static void addPressureTooltip(ItemStack stack, List<ITextComponent> textList) {
        stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            TextFormatting color;
            if (f < 0.1f) {
                color = TextFormatting.RED;
            } else if (f < 0.5f) {
                color = TextFormatting.GOLD;
            } else {
                color = TextFormatting.DARK_GREEN;
            }
            textList.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1)).withStyle(color));
        });
    }

    private static void handleProgrammableTooltip(ItemTooltipEvent event) {
        IProgrammable programmable = (IProgrammable) event.getItemStack().getItem();
        if (programmable.canProgram(event.getItemStack()) && programmable.showProgramTooltip()) {
            boolean hasInvalidPrograms = false;
            List<ITextComponent> addedEntries = new ArrayList<>();
            List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(event.getItemStack());
            Map<ResourceLocation, Integer> widgetMap = getPuzzleSummary(widgets);
            for (Map.Entry<ResourceLocation, Integer> entry : widgetMap.entrySet()) {
                TextFormatting[] prefix = new TextFormatting[0];
                ProgWidgetType<?> widgetType = ModProgWidgets.PROG_WIDGETS.get().getValue(entry.getKey());
                Screen curScreen = Minecraft.getInstance().screen;
                if (curScreen instanceof IGuiDrone) {
                    if (!((IGuiDrone) curScreen).getDrone().isProgramApplicable(widgetType)) {
                        prefix = new TextFormatting[]{ TextFormatting.RED, TextFormatting.ITALIC };
                        hasInvalidPrograms = true;
                    }
                }
                addedEntries.add(new StringTextComponent(Symbols.BULLET + " " + entry.getValue() + " x ")
                        .append(xlate(widgetType.getTranslationKey()))
                        .withStyle(prefix));
            }
            if (hasInvalidPrograms) {
                event.getToolTip().add(xlate("pneumaticcraft.gui.tooltip.programmable.invalidPieces").withStyle(TextFormatting.RED));
            }
            addedEntries.sort(Comparator.comparing(ITextComponent::getString));
            event.getToolTip().addAll(addedEntries);
            if (ClientUtils.hasShiftDown() && !widgets.isEmpty()) {
                event.getToolTip().add(xlate("pneumaticcraft.gui.tooltip.programmable.requiredPieces", widgets.size()).withStyle(TextFormatting.GREEN));
            }
        }
    }

    private static void handleFluidContainerTooltip(ItemTooltipEvent event) {
        FluidUtil.getFluidContained(event.getItemStack()).ifPresent(fluidStack -> {
            String key = ICustomTooltipName.getTranslationKey(event.getItemStack(), true);
            if (I18n.exists(key)) {
//                if (event.getToolTip().get(event.getToolTip().size() - 1).getString().contains("Minecraft Forge")) {
//                    // bit of a kludge!  otherwise the blue "Minecraft Forge" string gets shown twice
//                    event.getToolTip().remove(event.getToolTip().size() - 1);
//                }
//                String prefix = "";
//                if (!fluidStack.getFluid().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
//                    // fluid is owned by another mod; let's make it clear that this tooltip applies to PneumaticCraft
//                    prefix = TextFormatting.DARK_AQUA + "" + TextFormatting.ITALIC + "[" + Names.MOD_NAME + "] ";
//                }
                if (Screen.hasShiftDown()) {
                    String translatedInfo = TextFormatting.AQUA + I18n.get(key);
                    event.getToolTip().addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.splitString(/*prefix +*/ translatedInfo)));
                } else {
                    event.getToolTip().add(xlate("pneumaticcraft.gui.tooltip.sneakForInfo").withStyle(TextFormatting.AQUA));
                }
            }
        });
    }

    private static Map<ResourceLocation, Integer> getPuzzleSummary(List<IProgWidget> widgets) {
        Map<ResourceLocation, Integer> map = new HashMap<>();
        for (IProgWidget widget : widgets) {
            map.put(widget.getTypeID(), map.getOrDefault(widget.getTypeID(), 0) + 1);
        }
        return map;
    }


    @SubscribeEvent
    public static void renderTooltipEvent(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof ItemMicromissiles
                && stack.hasTag()
                && ItemMicromissiles.getFireMode(stack) == ItemMicromissiles.FireMode.SMART)
        {
            int width = 0;
            FontRenderer fr = event.getFontRenderer();
            int vSpace = fr.lineHeight + 1;
            int y = event.getY() + vSpace * 2 + 2;
            MatrixStack matrixStack = event.getMatrixStack();
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 500);
            width = Math.max(width, fr.width(I18n.get("pneumaticcraft.gui.micromissile.topSpeed")));
            width = Math.max(width, fr.width(I18n.get("pneumaticcraft.gui.micromissile.turnSpeed")));
            width = Math.max(width, fr.width(I18n.get("pneumaticcraft.gui.micromissile.damage")));
            matrixStack.popPose();

            int barX = event.getX() + width + 2;
            int barW = event.getWidth() - width - 10;
            RenderSystem.disableTexture();
            RenderSystem.lineWidth(10);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(1, (short) 0xFEFE);
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            drawLine(matrixStack, barX, y, (int) (barW * NBTUtils.getFloat(stack, ItemMicromissiles.NBT_TOP_SPEED)));
            drawLine(matrixStack, barX, y + vSpace, (int) (barW * NBTUtils.getFloat(stack, ItemMicromissiles.NBT_TURN_SPEED)));
            drawLine(matrixStack, barX, y + 2 * vSpace, (int) (barW * NBTUtils.getFloat(stack, ItemMicromissiles.NBT_DAMAGE)));
            RenderSystem.lineWidth(1);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            RenderSystem.shadeModel(GL11.GL_FLAT);
        }
    }

    private static void drawLine(MatrixStack matrixStack, int x, int y, int length) {
        BufferBuilder bb = Tessellator.getInstance().getBuilder();
        Matrix4f posMat = matrixStack.last().pose();
        bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bb.vertex(posMat, x, y + 4, 0).color(128, 128, 0, 255).endVertex();
        bb.vertex(posMat, x + length, y + 4, 0).color(0, 192, 0, 255).endVertex();
        Tessellator.getInstance().end();
    }
}
