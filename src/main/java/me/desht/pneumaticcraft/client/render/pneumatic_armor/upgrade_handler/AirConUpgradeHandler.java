package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.google.common.base.Strings;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiAirConditionerOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AirConUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    private static final int MAX_AC = 20;

    public static int deltaTemp;  // set by packet from server
    private static int currentAC = 0; // cosmetic

    @OnlyIn(Dist.CLIENT)
    private GuiAnimatedStat acStat;

    @Override
    public String getUpgradeName() {
        return "airConditioning";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { EnumUpgrade.AIR_CONDITIONING.getItem() };
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiAirConditionerOptions(this);
    }

    @Override
    public void update(PlayerEntity player, int rangeUpgrades) {
        super.update(player, rangeUpgrades);

        if ((player.world.getGameTime() & 0x3) == 0) {
            if (currentAC < deltaTemp)
                currentAC++;
            else if (currentAC > deltaTemp)
                currentAC--;
        }

        if (acStat.isClicked()) {
            int ac = MathHelper.clamp(currentAC, -MAX_AC, MAX_AC);
            String bar = (ac < 0 ? TextFormatting.BLUE : TextFormatting.GOLD)
                    + Strings.repeat("|", Math.abs(ac))
                    + TextFormatting.DARK_GRAY
                    + Strings.repeat("|", MAX_AC - Math.abs(ac));
            acStat.setTitle(TextFormatting.YELLOW + "A/C: " + bar);
            acStat.setBackGroundColor(ac < 0 ? 0x300080FF : (ac == 0 ? 0x3000AA00 : 0x30FFD000));
        }
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (acStat == null) {
            acStat = new GuiAnimatedStat(null, "", GuiAnimatedStat.StatIcon.NONE,
                    0x3000AA00, null, ArmorHUDLayout.INSTANCE.airConStat);
            acStat.setMinDimensionsAndReset(0, 0);
        }
        return acStat;
    }

    @Override
    public void onResolutionChanged() {
        acStat = null;
    }
}
