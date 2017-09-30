package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelGauge;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ModulePressureGauge extends TubeModuleRedstoneEmitting {
    @SideOnly(Side.CLIENT)
    private final ModelGauge model = new ModelGauge(this);

    public ModulePressureGauge() {
        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public void update() {
        super.update();
        if (!pressureTube.world().isRemote) {
            if (pressureTube.world().getTotalWorldTime() % 20 == 0)
                NetworkHandler.sendToAllAround(new PacketUpdatePressureBlock((TileEntityPneumaticBase) getTube()), getTube().world());
            setRedstone(getRedstone(pressureTube.getAirHandler(null).getPressure()));
        }
    }

    private int getRedstone(float pressure) {
        return (int) ((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks) {
        model.renderModel(0.0625f, dir, partialTicks);
    }

    @Override
    public String getType() {
        return Names.MODULE_GAUGE;
    }

    @Override
    public String getModelName() {
        return "gauge_module";
    }

    @Override
    public double getWidth() {
        return 0.5;
    }

    @Override
    protected double getHeight() {
        return 0.25;
    }

    @Override
    public void addItemDescription(List<String> curInfo) {
        curInfo.add(TextFormatting.BLUE + "Formula: Redstone = 2.0 x pressure(bar)");
        curInfo.add("This module emits a redstone signal, the strength of");
        curInfo.add("which depends the tube's pressure.");
    }

    @Override
    public boolean onActivated(EntityPlayer player) {
        return super.onActivated(player);
    }

    @Override
    protected EnumGuiId getGuiId() {
        return EnumGuiId.PRESSURE_MODULE;
    }
}
