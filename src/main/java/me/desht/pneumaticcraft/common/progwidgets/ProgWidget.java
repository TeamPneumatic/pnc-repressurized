package me.desht.pneumaticcraft.common.progwidgets;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidget implements IProgWidget {
    private final ProgWidgetType<?> type;
    private int x, y;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;

    public ProgWidget(ProgWidgetType<?> type) {
        this.type = type;
        if (!getParameters().isEmpty())
            connectedParameters = new IProgWidget[getParameters().size() * 2]; //times two because black- and whitelist.
    }

    public ProgWidgetType<?> getType() {
        return type;
    }

    @Override
    public ResourceLocation getTypeID() {
        return getType().getRegistryName();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        curTooltip.add(xlate(getTranslationKey()).applyTextStyle(TextFormatting.DARK_AQUA));
    }

    public String getExtraStringInfo() {
        return null;
    }

    @Override
    public void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        if (this instanceof IVariableWidget) {
            Set<String> variables = new HashSet<>();
            ((IVariableWidget) this).addVariables(variables);
            for (String variable : variables) {
                if (!variable.equals("") && !variable.startsWith("#") && !variable.startsWith("$") && !isVariableSetAnywhere(widgets, variable)) {
                    curInfo.add(xlate("gui.progWidget.general.warning.variableNeverSet", variable));
                }
            }
        }
    }

    private boolean isVariableSetAnywhere(List<IProgWidget> widgets, String variable) {
        if (variable.equals("")) return true;
        for (IProgWidget widget : widgets) {
            if (widget instanceof IVariableSetWidget) {
                Set<String> variables = new HashSet<>();
                ((IVariableSetWidget) widget).addVariables(variables);
                if (variables.contains(variable)) return true;
            }
        }
        return false;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        if (!hasStepInput() && hasStepOutput() && outputStepConnection == null) {
            curInfo.add(xlate("gui.progWidget.general.error.noPieceConnected"));
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return 30;
    }

    @Override
    public int getHeight() {
        return !getParameters().isEmpty() ? getParameters().size() * 22 : 22;
    }

    @Override
    public void setParent(IProgWidget widget) {
        parent = widget;
    }

    @Override
    public IProgWidget getParent() {
        return parent;
    }

    @Override
    public void render() {
        Minecraft.getInstance().getTextureManager().bindTexture(getTexture());
        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);//(getParameters() != null && getParameters().size() > 0 ? 10 : 0);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        Pair<Double, Double> maxUV = getMaxUV();
        double u = maxUV.getLeft();
        double v = maxUV.getRight();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, 0, 0).tex(0, 0).endVertex();
        wr.pos(0, height, 0).tex(0, v).endVertex();
        wr.pos(width, height, 0).tex(u, v).endVertex();
        wr.pos(width, 0, 0).tex(u, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    public void renderExtraInfo() {
        if (getExtraStringInfo() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scaled(0.5, 0.5, 0.5);
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            String[] splittedInfo = WordUtils.wrap(getExtraStringInfo(), 40).split(System.getProperty("line.separator"));
            for (int i = 0; i < splittedInfo.length; i++) {
                int stringLength = fr.getStringWidth(splittedInfo[i]);
                int startX = getWidth() / 2 - stringLength / 4;
                int startY = getHeight() / 2 - (fr.FONT_HEIGHT + 1) * (splittedInfo.length - 1) / 4 + (fr.FONT_HEIGHT + 1) * i / 2 - fr.FONT_HEIGHT / 4;
                AbstractGui.fill(startX * 2 - 1, startY * 2 - 1, startX * 2 + stringLength + 1, startY * 2 + fr.FONT_HEIGHT + 1, 0xFFFFFFFF);
                fr.drawString(splittedInfo[i], startX * 2, startY * 2, 0xFF000000);
            }
            GlStateManager.popMatrix();
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

    @Override
    public Pair<Double, Double> getMaxUV() {
        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        int textureSize = getTextureSize();
        double u = (double) width / textureSize;
        double v = (double) height / textureSize;
        return new ImmutablePair<>(u, v);
    }

    @Override
    public int getTextureSize() {
        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        int maxSize = Math.max(width, height);

        int textureSize = 1;
        while (textureSize < maxSize) {
            textureSize *= 2;
        }
        return textureSize;
    }

    @Override
    public boolean hasStepOutput() {
        return hasStepInput();
    }

    @Override
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public void setParameter(int index, IProgWidget parm) {
        int index2 = index >= getParameters().size() ? index - getParameters().size() : index;
        if (connectedParameters != null && (parm == null || parm.getType() == getParameters().get(index2)))
            connectedParameters[index] = parm;
    }

    @Override
    public boolean canSetParameter(int index) {
        if (connectedParameters != null) {
            return hasBlacklist() || index < connectedParameters.length / 2;
        }
        return false;
    }

    protected boolean hasBlacklist() {
        return true;
    }

    @Override
    public IProgWidget[] getConnectedParameters() {
        return connectedParameters;
    }

    @Override
    public void setOutputWidget(IProgWidget widget) {
        outputStepConnection = widget;
    }

    @Override
    public IProgWidget getOutputWidget() {
        return outputStepConnection;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        return outputStepConnection;
    }

    @Override
    public IProgWidget copy() {
        IProgWidget copy = getType().create();
        CompoundNBT tag = new CompoundNBT();
        writeToNBT(tag);
        copy.readFromNBT(tag);
        return copy;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putString("name", getTypeID().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        x = tag.getInt("x");
        y = tag.getInt("y");
    }

    static List getConnectedWidgetList(IProgWidget widget, int parameterIndex) {
        IProgWidget connectingWidget = widget.getConnectedParameters()[parameterIndex];
        if (connectingWidget != null) {
            List<IProgWidget> list = new ArrayList<>();
            while (connectingWidget != null) {
                list.add(connectingWidget);
                connectingWidget = connectingWidget.getConnectedParameters()[0];
            }
            return list;
        } else {
            return null;
        }
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget) {
        return getWidgetAI(drone, widget) != null;
    }

}
