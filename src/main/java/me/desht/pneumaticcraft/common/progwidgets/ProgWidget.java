package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ProgWidget implements IProgWidget {
    private int x, y;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;

    // private static Gui gui;
    public ProgWidget() {
        if (getParameters() != null)
            connectedParameters = new IProgWidget[getParameters().length * 2]; //times two because black- and whitelist.
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        curTooltip.add(TextFormatting.DARK_AQUA + I18n.format("programmingPuzzle." + getWidgetString() + ".name"));
    }

    @Override
    public void renderExtraInfo() {
        if (getExtraStringInfo() != null) {
            GL11.glPushMatrix();
            GL11.glScaled(0.5, 0.5, 0.5);
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            String[] splittedInfo = WordUtils.wrap(getExtraStringInfo(), 40).split(System.getProperty("line.separator"));
            for (int i = 0; i < splittedInfo.length; i++) {
                int stringLength = fr.getStringWidth(splittedInfo[i]);
                int startX = getWidth() / 2 - stringLength / 4;
                int startY = getHeight() / 2 - (fr.FONT_HEIGHT + 1) * (splittedInfo.length - 1) / 4 + (fr.FONT_HEIGHT + 1) * i / 2 - fr.FONT_HEIGHT / 4;
                Gui.drawRect(startX * 2 - 1, startY * 2 - 1, startX * 2 + stringLength + 1, startY * 2 + fr.FONT_HEIGHT + 1, 0xFFFFFFFF);
                fr.drawString(splittedInfo[i], startX * 2, startY * 2, 0xFF000000);
            }
            GL11.glPopMatrix();
            GL11.glColor4d(1, 1, 1, 1);
        }
    }

    public String getExtraStringInfo() {
        return null;
    }

    @Override
    public void addWarnings(List<String> curInfo, List<IProgWidget> widgets) {
        if (this instanceof IVariableWidget) {
            Set<String> variables = new HashSet<String>();
            ((IVariableWidget) this).addVariables(variables);
            for (String variable : variables) {
                if (!variable.equals("") && !variable.startsWith("#") && !variable.startsWith("$") && !isVariableSetAnywhere(widgets, variable)) {
                    curInfo.add(I18n.format("gui.progWidget.general.warning.variableNeverSet", variable));
                }
            }
        }
    }

    private boolean isVariableSetAnywhere(List<IProgWidget> widgets, String variable) {
        if (variable.equals("")) return true;
        for (IProgWidget widget : widgets) {
            if (widget instanceof IVariableSetWidget) {
                Set<String> variables = new HashSet<String>();
                ((IVariableSetWidget) widget).addVariables(variables);
                if (variables.contains(variable)) return true;
            }
        }
        return false;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        if (!hasStepInput() && hasStepOutput() && outputStepConnection == null) {
            curInfo.add("gui.progWidget.general.error.noPieceConnected");
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
        return getParameters() != null ? getParameters().length * 22 : 22;
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
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
        int width = getWidth() + (getParameters() != null && getParameters().length > 0 ? 10 : 0);
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
    public Pair<Double, Double> getMaxUV() {
        int width = getWidth() + (getParameters() != null && getParameters().length > 0 ? 10 : 0);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        int textureSize = getTextureSize();
        double u = (double) width / textureSize;
        double v = (double) height / textureSize;
        return new ImmutablePair<Double, Double>(u, v);
    }

    @Override
    public int getTextureSize() {
        int width = getWidth() + (getParameters() != null && getParameters().length > 0 ? 10 : 0);
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
    public EntityAIBase getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public void setParameter(int index, IProgWidget parm) {
        if (connectedParameters != null && (parm == null || parm.getClass() == getParameters()[index]))
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
        try {
            IProgWidget copy = this.getClass().newInstance();
            NBTTagCompound tag = new NBTTagCompound();
            writeToNBT(tag);
            copy.readFromNBT(tag);
            return copy;
        } catch (Exception e) {
            Log.error("Error occured when trying to copy an " + getWidgetString() + " widget.");
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("name", getWidgetString());
        tag.setInteger("x", x);
        tag.setInteger("y", y);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        x = tag.getInteger("x");
        y = tag.getInteger("y");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return null;
    }

    public static List getConnectedWidgetList(IProgWidget widget, int parameterIndex) {
        IProgWidget connectingWidget = widget.getConnectedParameters()[parameterIndex];
        if (connectingWidget != null) {
            List list = new ArrayList();
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
