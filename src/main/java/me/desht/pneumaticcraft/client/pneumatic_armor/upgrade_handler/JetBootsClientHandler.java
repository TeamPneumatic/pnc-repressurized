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

package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.JetBootsOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsActivate;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JetBootsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Collection;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JetBootsClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<JetBootsHandler> {
    private static final ItemStack PICK = new ItemStack(Items.DIAMOND_PICKAXE);
    private static final ItemStack ROTOR = new ItemStack(ModItems.TURBINE_ROTOR.get());
    private static final ItemStack ELYTRA = new ItemStack(Items.ELYTRA);
    private static final ItemStack FEATHER = new ItemStack(Items.FEATHER);

    private static final String[] HEADINGS = new String[] { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };

    public static final ResourceLocation MODULE_BUILDER_MODE = RL("jet_boots.module.builder_mode");
    public static final ResourceLocation MODULE_FLIGHT_STABILIZERS = RL("jet_boots.module.flight_stabilizers");
    public static final ResourceLocation MODULE_HOVER = RL("jet_boots.module.hover");
    public static final ResourceLocation MODULE_SMART_HOVER = RL("jet_boots.module.smart_hover");

    private static final StatPanelLayout DEFAULT_STAT_LAYOUT = new StatPanelLayout(0.5f, 0.005f, false);

    private String l1, l2, l3, r1, r2, r3;
    private int widestR;
    private boolean builderMode;
    private boolean flightStabilizers;
    private boolean smartHover;
    private double prevX, prevY, prevZ;

    private IGuiAnimatedStat jbStat;

    public JetBootsClientHandler() {
        super(CommonUpgradeHandlers.jetBootsHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new JetBootsOptions(screen,this);
    }

    @Override
    public boolean isEnabledByDefault(String subModuleName) {
        return subModuleName.equals(MODULE_HOVER.getPath());
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
        super.tickClient(armorHandler, isEnabled);

        JetBootsHandler jbHandler = CommonUpgradeHandlers.jetBootsHandler;
        JetBootsStateTracker.JetBootsState jbState = jbHandler.getJetBootsSyncedState(armorHandler);
        if (armorHandler.upgradeUsable(jbHandler, false)) {
            if (jbState.isActive() && (!jbState.isEnabled() || !thrustKeyPressed(jbState.isBuilderMode()))) {
                NetworkHandler.sendToServer(new PacketJetBootsActivate(false));
                jbHandler.setJetBootsActive(armorHandler, false);
            } else if (!jbState.isActive() && jbState.isEnabled() && thrustKeyPressed(jbState.isBuilderMode())) {
                NetworkHandler.sendToServer(new PacketJetBootsActivate(true));
                jbHandler.setJetBootsActive(armorHandler, true);
            }
        }

        if (!isEnabled) return;

        String g1 = ChatFormatting.WHITE.toString();
        String g2 = ChatFormatting.GREEN.toString();

        if (jbStat.isStatOpen()) {
            Player player = armorHandler.getPlayer();
            double mx = player.getX() - prevX;
            double my = player.getY() - prevY;
            double mz = player.getZ() - prevZ;
            prevX = player.getX();
            prevY = player.getY();
            prevZ = player.getZ();
            double v = Math.sqrt(mx * mx + my * my + mz * mz);
            double vg = Math.sqrt(mx * mx + mz * mz);
            int heading = Mth.floor((double)(player.getYRot() * 8.0F / 360.0F) + 0.5D) & 0x7;
            int yaw = ((int) player.getYRot() + 180) % 360;
            if (yaw < 0) yaw += 360;
            BlockPos pos = player.blockPosition();

            l1 = String.format(" %sSpd: %s%05.2fm/s", g1, g2, v * 20);
            l2 = String.format("  %sAlt: %s%03dm", g1, g2, pos.getY());
            l3 = String.format("%sHead: %s%d° (%s)", g1, g2, yaw, HEADINGS[heading]);
            r1 = String.format("%sGnd: %s%05.2f", g1, g2, vg * 20);
            r2 = String.format("%sGnd: %s%dm", g1, g2, pos.getY() - player.level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()));
            r3 = String.format("%sPch: %s%d°", g1, g2, (int)-player.getXRot());
            Font fr = Minecraft.getInstance().font;
            widestR = Math.max(fr.width(r1), Math.max(fr.width(r2), fr.width(r3)));

            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            builderMode = jbState.isBuilderMode();

            JetBootsHandler.JetBootsLocalState jbLocal = handler.getExtensionData(getCommonHandler());
            flightStabilizers = jbLocal.isFlightStabilizers();
            smartHover = jbLocal.isSmartHover();
        }
    }

    @Override
    public void render2D(PoseStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
        if (armorPieceHasPressure && jbStat.isStatOpen()) {
            Font fr = Minecraft.getInstance().font;
            int xl = jbStat.getBaseX() + 5;
            if (xl > Minecraft.getInstance().getWindow().getGuiScaledWidth()) return;
            int y = jbStat.getBaseY() + fr.lineHeight + 8;
            int xr = jbStat.getBaseX() + jbStat.getStatWidth() - 5;
            if (jbStat.isLeftSided()) {
                xl -= jbStat.getStatWidth();
                xr -= jbStat.getStatWidth();
            }
            fr.drawShadow(matrixStack, l1, xl, y, 0x404040);
            fr.drawShadow(matrixStack, l2, xl, y + fr.lineHeight, 0x404040);
            fr.drawShadow(matrixStack, l3, xl, y + fr.lineHeight * 2, 0x404040);
            fr.drawShadow(matrixStack, r1, xr - widestR, y, 0x404040);
            fr.drawShadow(matrixStack, r2, xr - widestR, y + fr.lineHeight, 0x404040);
            fr.drawShadow(matrixStack, r3, xr - widestR, y + fr.lineHeight * 2, 0x404040);

            int iconX = xr - 30;
            if (builderMode) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(PICK, iconX, jbStat.getBaseY());
                iconX -= 16;
            }
            if (flightStabilizers) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(ROTOR, iconX, jbStat.getBaseY());
                iconX -= 16;
            }
            if (ClientUtils.getClientPlayer().isFallFlying()) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(ELYTRA, iconX, jbStat.getBaseY());
                iconX -= 16;
            }
            if (smartHover) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(FEATHER, iconX, jbStat.getBaseY());
//                iconX -= 16;
            }
        }
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (jbStat == null) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                prevX = player.getX();
                prevY = player.getY();
                prevZ = player.getZ();
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            int n = Math.max(1, handler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()));
            ItemStack stack = new ItemStack(ModUpgrades.JET_BOOTS.get().getItem(n));
            jbStat = ClientArmorRegistry.getInstance().makeHUDStatPanel(xlate(IArmorUpgradeHandler.getStringKey(getID())), stack, this);
            jbStat.setMinimumContractedDimensions(0, 0);
            jbStat.setMinimumExpandedDimensions(120, 42);
        }
        return jbStat;
    }

    @Override
    public StatPanelLayout getDefaultStatLayout() {
        return DEFAULT_STAT_LAYOUT;
    }

    @Override
    public void onResolutionChanged() {
        jbStat = null;
    }

    @Override
    public Collection<ResourceLocation> getSubKeybinds() {
        return ImmutableList.of(
                MODULE_BUILDER_MODE,
                MODULE_FLIGHT_STABILIZERS,
                MODULE_HOVER,
                MODULE_SMART_HOVER
        );
    }

    private static boolean thrustKeyPressed(boolean builderMode) {
        return KeyHandler.getInstance().keybindJetBoots.isDown() || builderMode && Minecraft.getInstance().options.keyJump.isDown();
    }
}
