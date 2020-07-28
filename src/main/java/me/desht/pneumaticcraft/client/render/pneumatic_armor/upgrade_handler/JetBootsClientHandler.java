package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.JetBootsOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.Heightmap;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JetBootsClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler {
    public static final int BUILDER_MODE_LEVEL = 3;  // tier needed for builder mode

    private static final String[] HEADINGS = new String[] { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };

    private String l1, l2, l3, r1, r2, r3;
    private int widestR;
    private boolean drawShovel;

    private IGuiAnimatedStat jbStat;

    public JetBootsClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().jetBootsHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new JetBootsOptions(screen,this);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        super.tickClient(armorHandler);

        String g1 = TextFormatting.WHITE.toString();
        String g2 = TextFormatting.GREEN.toString();

        PlayerEntity player = armorHandler.getPlayer();
        if (jbStat.isStatOpen()) {
            double mx = player.getPosX() - player.prevPosX;
            double my = player.getPosY() - player.prevPosY;
            double mz = player.getPosZ() - player.prevPosZ;
            double v = Math.sqrt(mx * mx + my * my + mz * mz);
            double vg = Math.sqrt(mx * mx + mz * mz);
            int heading = MathHelper.floor((double)(player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 0x7;
            int yaw = ((int) player.rotationYaw + 180) % 360;
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

            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            drawShovel = handler.isJetBootsBuilderMode();
        }
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled) {
        super.render2D(matrixStack, partialTicks, helmetEnabled);

        if (helmetEnabled && jbStat.isStatOpen()) {
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            int xl = jbStat.getBaseX() + 5;
            int y = jbStat.getBaseY() + fr.FONT_HEIGHT + 8;
            int xr = jbStat.getBaseX() + jbStat.getStatWidth() - 5;
            if (jbStat.isLeftSided()) {
                xl -= jbStat.getStatWidth();
                xr -= jbStat.getStatWidth();
            }
            fr.drawStringWithShadow(matrixStack, l1, xl, y, 0x404040);
            fr.drawStringWithShadow(matrixStack, l2, xl, y + fr.FONT_HEIGHT, 0x404040);
            fr.drawStringWithShadow(matrixStack, l3, xl, y + fr.FONT_HEIGHT * 2, 0x404040);
            fr.drawStringWithShadow(matrixStack, r1, xr - widestR, y, 0x404040);
            fr.drawStringWithShadow(matrixStack, r2, xr - widestR, y + fr.FONT_HEIGHT, 0x404040);
            fr.drawStringWithShadow(matrixStack, r3, xr - widestR, y + fr.FONT_HEIGHT * 2, 0x404040);

            if (drawShovel) {
                GuiUtils.renderItemStack(matrixStack, new ItemStack(Items.DIAMOND_PICKAXE), xr - 30, jbStat.getBaseY());
            }
        }
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (jbStat == null) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            int n = Math.max(1, handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS));
            ItemStack stack = new ItemStack(EnumUpgrade.JET_BOOTS.getItem(n));
            jbStat = new WidgetAnimatedStat(null, xlate(ArmorUpgradeRegistry.getStringKey(getCommonHandler().getID())),
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
