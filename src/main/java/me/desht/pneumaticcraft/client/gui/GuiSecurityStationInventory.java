package me.desht.pneumaticcraft.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.render.RenderHackSimulation;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationMain;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation.EnumNetworkValidityProblem;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiSecurityStationInventory extends GuiPneumaticContainerBase<ContainerSecurityStationMain, TileEntitySecurityStation> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat accessStat;

    private WidgetButtonExtended addUserButton;
    private Button rebootButton;
    private WidgetTextField sharedUserTextField;
    private List<WidgetButtonExtended> sharedUserList;

    // for cosmetic purposes only; draws the animated connection lines between nodes
    private RenderHackSimulation hackRenderer;
    private HackSimulation hackSimulation;
    private boolean reInitBG;

    public GuiSecurityStationInventory(ContainerSecurityStationMain container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        imageHeight = 239;
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.securityStation.status"),
                new ItemStack(ModBlocks.SECURITY_STATION.get()), 0xFFD08000, false);
        accessStat = addAnimatedStat(xlate("pneumaticcraft.gui.securityStation.sharedUsers"),
                new ItemStack(Items.PLAYER_HEAD), 0xFF005500, false);

        Rectangle2d accessButtonRectangle = new Rectangle2d(105, 12, 16, 16);
        addUserButton = getButtonFromRectangle(null, accessButtonRectangle, "+", b -> {
            if (!sharedUserTextField.getValue().isEmpty()) {
                NetworkHandler.sendToServer(new PacketGuiButton("add:" + sharedUserTextField.getValue()));
                sharedUserTextField.setValue("");
            }
        });

        sharedUserTextField = new WidgetTextField(font, 20, 15, 80, 10);

        accessStat.addSubWidget(sharedUserTextField);
        accessStat.addSubWidget(addUserButton);
        accessStat.setMinimumExpandedDimensions(125, 40);

        addButton(rebootButton = new WidgetButtonExtended(leftPos + 110, topPos + 17, 60, 20, xlate("pneumaticcraft.gui.securityStation.reboot")).withTag("reboot"));
        addButton(new WidgetButtonExtended(leftPos + 110, topPos + 107, 60, 20, xlate("pneumaticcraft.gui.securityStation.test")))
                .withTag("test");
        addButton(new WidgetRangeToggleButton(leftPos + 154, topPos + 130, te));

        updateUserList();
        initConnectionRendering();
    }

    private void initConnectionRendering() {
        hackRenderer = new RenderHackSimulation(leftPos + 25, topPos + 27, 18);
        hackSimulation = HackSimulation.dummySimulation();
        hackSimulation.wakeUp();
        for (int i = 0; i < te.getPrimaryInventory().getSlots(); i++) {
            hackSimulation.addNode(i, te.getPrimaryInventory().getStackInSlot(i));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SECURITY_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float opacity, int x, int y) {
        super.renderBg(matrixStack, opacity, x, y);
        hackRenderer.render(matrixStack, hackSimulation, 0xFF2222FF);
    }

    @Override
    public void tick() {
        super.tick();

        if (reInitBG) {
            initConnectionRendering();
            reInitBG = false;
        }

        hackSimulation.tick();

        statusStat.setText(getStatusText());
        accessStat.setText(getAccessText());
        ITextComponent rebootButtonString;
        if (te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 20 ?
                    xlate("pneumaticcraft.gui.securityStation.rebooting") :
                    new StringTextComponent(PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false));
        } else {
            rebootButtonString = xlate("pneumaticcraft.gui.securityStation.reboot").withStyle(TextFormatting.RED);
        }
        rebootButton.setMessage(rebootButtonString);

        addUserButton.visible = accessStat.isDoneExpanding();
        for (WidgetButtonExtended button : sharedUserList) {
            button.active = accessStat.isDoneExpanding();
        }
        if (sharedUserList.size() != te.sharedUsers.size()) {
            updateUserList();
        }

        rebootButton.active = te.getRebootTime() == 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && sharedUserTextField.isFocused() && !sharedUserTextField.getValue().isEmpty()) {
            NetworkHandler.sendToServer(new PacketGuiButton("add:" + sharedUserTextField.getValue()));
            sharedUserTextField.setValue("");
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> text) {
        super.addProblems(text);

        if (te.getRebootTime() > 0) {
            text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.security_station.rebooting", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false)));
        } else if (te.isHacked()) {
            text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.security_station.hacked"));
        }
        if (!te.hasValidNetwork()) {
            text.add(xlate("pneumaticcraft.gui.tab.problems.security_station.invalidNetwork").withStyle(TextFormatting.WHITE));
            EnumNetworkValidityProblem problem = te.checkForNetworkValidity();
            if (problem != EnumNetworkValidityProblem.NONE) text.addAll(GuiUtils.xlateAndSplit(problem.getTranslationKey()));
        }
    }

    private List<ITextComponent> getStatusText() {
        List<ITextComponent> text = new ArrayList<>();
        StringTextComponent space = new StringTextComponent("  ");
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.protection").withStyle(TextFormatting.WHITE));
        if (te.getRebootTime() > 0) {
            text.add(new StringTextComponent("  ").append(xlate("pneumaticcraft.gui.securityStation.rebooting")).withStyle(TextFormatting.DARK_RED));
        } else if (te.isHacked()) {
            text.add(new StringTextComponent("  ").append(xlate("pneumaticcraft.gui.tab.status.securityStation.hackedBy")).withStyle(TextFormatting.DARK_RED));
            for (GameProfile hacker : te.hackedUsers) {
                text.add(new StringTextComponent("  ").append(Symbols.bullet()).append(hacker.getName()).withStyle(TextFormatting.RED));
            }
        } else {
            text.add(new StringTextComponent("  ").append(xlate("pneumaticcraft.gui.tab.status.securityStation.secure")).withStyle(TextFormatting.GREEN));
        }
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityLevel").withStyle(TextFormatting.WHITE));
        text.add(new StringTextComponent("  ").append(new StringTextComponent("L" + te.getSecurityLevel())).withStyle(TextFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.detectChance").withStyle(TextFormatting.WHITE));
        text.add(new StringTextComponent("  ").append(new StringTextComponent(te.getDetectionChance() + "%")).withStyle(TextFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityRange").withStyle(TextFormatting.WHITE));
        text.add(new StringTextComponent("  ").append(new StringTextComponent((te.getRange() * 2 + 1) + "m²")).withStyle(TextFormatting.BLACK));
        return text;
    }

    private List<ITextComponent> getAccessText() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(StringTextComponent.EMPTY);
        textList.add(StringTextComponent.EMPTY);
        boolean first = true;
        List<String> names = te.sharedUsers.stream().map(GameProfile::getName).sorted().collect(Collectors.toList());
        for (String name : names) {
            String str = first ? name + " \u2654" : name;
            textList.add(Symbols.bullet().append(str).withStyle(first ? TextFormatting.YELLOW : TextFormatting.WHITE));
            first = false;
        }
        return textList;
    }

    private void updateUserList() {
        if (sharedUserList != null) {
            for (WidgetButtonExtended button : sharedUserList) {
                accessStat.removeSubWidget(button);
            }
        }
        sharedUserList = new ArrayList<>();
        int n = 0;
        List<String> names = te.sharedUsers.stream().map(GameProfile::getName).sorted().collect(Collectors.toList());
        for (String name : names) {
            Rectangle2d rect = new Rectangle2d(24, 30 + n * (font.lineHeight + 1), font.width(name), 8);
            WidgetButtonExtended button = getInvisibleButtonFromRectangle("remove:" + name, rect, b -> {});
            button.setInvisibleHoverColor(0x80FF0000);
            button.setVisible(false);
            accessStat.addSubWidget(button);
            sharedUserList.add(button);
            button.visible = !name.equals(minecraft.player.getGameProfile().getName());
            n++;
        }
    }

    public static void reinitConnectionRendering() {
        if (Minecraft.getInstance().screen instanceof GuiSecurityStationInventory) {
            ((GuiSecurityStationInventory) Minecraft.getInstance().screen).reInitBG = true;
        }
    }
}
