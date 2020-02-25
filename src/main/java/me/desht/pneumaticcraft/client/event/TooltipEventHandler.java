package me.desht.pneumaticcraft.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.gui.IGuiDrone;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.core.ModRegistries;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import java.util.stream.Collectors;

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
            ((IInventoryItem) stack.getItem()).getStacksInItem(stack, stacks);
            ITextComponent header = ((IInventoryItem) stack.getItem()).getInventoryHeader();
            if (header != null && !stacks.isEmpty()) {
                curInfo.add(header);
            }
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks.toArray(new ItemStack[0]));
        }

        String key = ICustomTooltipName.getTranslationKey(stack);
        if (I18n.hasKey(key)) {
            if (ClientUtils.hasShiftDown()) {
                String translatedInfo = TextFormatting.AQUA + I18n.format(key);
                curInfo.addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.splitString(translatedInfo, 50)));
                if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
                    curInfo.add(xlate("gui.tab.info.assistIGW"));
                }
            } else {
                curInfo.add(xlate("gui.tooltip.sneakForInfo").applyTextStyle(TextFormatting.AQUA));
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
            textList.add(xlate("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1)).applyTextStyle(color));
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
                ProgWidgetType widgetType = ModRegistries.PROG_WIDGETS.getValue(entry.getKey());
                Screen curScreen = Minecraft.getInstance().currentScreen;
                if (curScreen instanceof IGuiDrone) {
                    if (!((IGuiDrone) curScreen).getDrone().isProgramApplicable(widgetType)) {
                        prefix = new TextFormatting[]{ TextFormatting.RED, TextFormatting.ITALIC };
                        hasInvalidPrograms = true;
                    }
                }
                addedEntries.add(new StringTextComponent(GuiConstants.BULLET + " " + entry.getValue() + " x ")
                        .appendSibling(xlate(widgetType.getTranslationKey()))
                        .applyTextStyles(prefix));
            }
            if (hasInvalidPrograms) {
                event.getToolTip().add(xlate("gui.tooltip.programmable.invalidPieces").applyTextStyle(TextFormatting.RED));
            }
            addedEntries.sort(Comparator.comparing(ITextComponent::getFormattedText));
            event.getToolTip().addAll(addedEntries);
            if (ClientUtils.hasShiftDown() && !widgets.isEmpty()) {
                event.getToolTip().add(xlate("gui.tooltip.programmable.requiredPieces", widgets.size()).applyTextStyles(TextFormatting.GREEN));
            }
        }
    }

    private static void handleFluidContainerTooltip(ItemTooltipEvent event) {
        FluidUtil.getFluidContained(event.getItemStack()).ifPresent(fluidStack -> {
            String key = "gui.tooltip.item.pneumaticcraft." + event.getItemStack().getItem().getRegistryName().getPath();
            if (I18n.hasKey(key)) {
                if (event.getToolTip().get(event.getToolTip().size() - 1).getFormattedText().contains("Minecraft Forge")) {
                    // bit of a kludge!  otherwise the blue "Minecraft Forge" string gets shown twice
                    event.getToolTip().remove(event.getToolTip().size() - 1);
                }
                String prefix = "";
                if (!fluidStack.getFluid().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
                    // fluid is owned by another mod; let's make it clear that this tooltip applies to PneumaticCraft
                    prefix = TextFormatting.DARK_AQUA + "" + TextFormatting.ITALIC + "[" + Names.MOD_NAME + "] ";
                }
                if (Screen.hasShiftDown()) {
                    String translatedInfo = TextFormatting.AQUA + I18n.format(key);
                    event.getToolTip().addAll(PneumaticCraftUtils.splitString(prefix + translatedInfo, 40).stream().map(StringTextComponent::new).collect(Collectors.toList()));
                } else {
                    event.getToolTip().add(xlate("gui.tooltip.sneakForInfo").applyTextStyles(TextFormatting.AQUA));
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
        if (stack.getItem() instanceof ItemMicromissiles && stack.hasTag()) {
            int width = 0;
            FontRenderer fr = event.getFontRenderer();
            int y = event.getY() + fr.FONT_HEIGHT * 2 + 5;
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.topSpeed")), event.getX(), y));
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.turnSpeed")), event.getX(), y + fr.FONT_HEIGHT));
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.damage")), event.getX(), y + fr.FONT_HEIGHT * 2));
            int barX = event.getX() + width + 2;
            int barW = event.getWidth() - width - 10;
            GlStateManager.disableTexture();
            GlStateManager.lineWidth(10);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(1, (short)0xFEFE);
            RenderUtils.glColorHex(0x00C000, 255);
            GlStateManager.begin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4);
            GL11.glVertex2i(barX + (int) (barW * NBTUtil.getFloat(stack, ItemMicromissiles.NBT_TOP_SPEED)), y + 4);
            GlStateManager.end();
            GlStateManager.begin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4 + fr.FONT_HEIGHT);
            GL11.glVertex2i(barX + (int) (barW * NBTUtil.getFloat(stack, ItemMicromissiles.NBT_TURN_SPEED)), y + 4 + fr.FONT_HEIGHT);
            GlStateManager.end();
            GlStateManager.begin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4 + fr.FONT_HEIGHT * 2);
            GL11.glVertex2i(barX + (int) (barW * NBTUtil.getFloat(stack, ItemMicromissiles.NBT_DAMAGE)), y + 4 + fr.FONT_HEIGHT * 2);
            GlStateManager.end();
            GlStateManager.lineWidth(1);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }
    }

    private static int renderString(FontRenderer fr, String s, int x, int y) {
        fr.drawStringWithShadow(s, x, y, 0xFFAAAAAA);
        return fr.getStringWidth(s);
    }

}
