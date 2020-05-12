package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiJetBootsOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JetBootsUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    public static final int BUILDER_MODE_LEVEL = 3;  // tier needed for builder mode

    private static final String[] HEADINGS = new String[] { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };

    private String l1, l2, l3, r1, r2, r3;
    private int widestR;

    @OnlyIn(Dist.CLIENT)
    private IGuiAnimatedStat jbStat;

    @Override
    public String getUpgradeID() {
        return "jetBoots";
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.JET_BOOTS };
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new GuiJetBootsOptions(screen,this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.FEET;
    }

    @Override
    public void tick(PlayerEntity player, int rangeUpgrades) {
        super.tick(player, rangeUpgrades);

        String g1 = TextFormatting.WHITE.toString();
        String g2 = TextFormatting.GREEN.toString();

        if (jbStat.isClicked()) {
            double mx = player.getPosX() - player.prevPosX;
            double my = player.getPosY() - player.prevPosY;
            double mz = player.getPosZ() - player.prevPosZ;
            double v = Math.sqrt(mx * mx + my * my + mz * mz);
            double vg = Math.sqrt(mx * mx + mz * mz);
            int heading = MathHelper.floor((double)(player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 0x7;
            int yaw = ((int)player.rotationYaw + 180) % 360;
            if (yaw < 0) yaw += 360;
            BlockPos pos = player.getPosition();

            l1 = String.format(" %sSpd: %s%05.2fm/s", g1, g2, v * 20);
            l2 = String.format("  %sAlt: %s%03dm", g1, g2, pos.getY());
            l3 = String.format("%sHead: %s%d° (%s)", g1, g2, yaw, HEADINGS[heading]);
            r1 = String.format("%sGnd: %s%05.2f", g1, g2, vg * 20);
            r2 = String.format("%sGnd: %s%dm", g1, g2, pos.getY() - player.world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()));
            r3 = String.format("%sPch: %s%d°", g1, g2, (int)-player.rotationPitch);
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            widestR = Math.max(fr.getStringWidth(r1), Math.max(fr.getStringWidth(r2), fr.getStringWidth(r3)));
        }
    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled) {
        super.render2D(partialTicks, helmetEnabled);

        if (helmetEnabled && jbStat.isClicked()) {
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            int xl = jbStat.getBaseX() + 5;
            int y = jbStat.getBaseY() + fr.FONT_HEIGHT + 8;
            int xr = jbStat.getBaseX() + jbStat.getWidth() - 5;
            if (jbStat.isLeftSided()) {
                xl -= jbStat.getWidth();
                xr -= jbStat.getWidth();
            }
            fr.drawStringWithShadow(l1, xl, y, 0x404040);
            fr.drawStringWithShadow(l2, xl, y + fr.FONT_HEIGHT, 0x404040);
            fr.drawStringWithShadow(l3, xl, y + fr.FONT_HEIGHT * 2, 0x404040);
            fr.drawStringWithShadow(r1, xr - widestR, y, 0x404040);
            fr.drawStringWithShadow(r2, xr - widestR, y + fr.FONT_HEIGHT, 0x404040);
            fr.drawStringWithShadow(r3, xr - widestR, y + fr.FONT_HEIGHT * 2, 0x404040);
        }
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (jbStat == null) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            int n = Math.max(1, handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS));
            ItemStack stack = new ItemStack(EnumUpgrade.JET_BOOTS.getItem(n));
            jbStat = new WidgetAnimatedStat(null, "Jet Boots",
                    WidgetAnimatedStat.StatIcon.of(stack),
                    0x3000AA00, null, ArmorHUDLayout.INSTANCE.jetBootsStat);
            jbStat.setMinDimensionsAndReset(0, 0);
            jbStat.addPadding(3, 32);
        }
        return jbStat;
    }

    @Override
    public void onResolutionChanged() {
        jbStat = null;
    }
}
