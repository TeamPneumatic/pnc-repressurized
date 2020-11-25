package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.google.common.base.Strings;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.AirConditionerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AirConClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler {
    private static final int MAX_AC = 20;

    public static int deltaTemp;  // set by packet from server
    private static int currentAC = 0; // cosmetic

    private IGuiAnimatedStat acStat;

    public AirConClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().airConHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new AirConditionerOptions(screen, this);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        super.tickClient(armorHandler);

        if ((armorHandler.getPlayer().world.getGameTime() & 0x3) == 0) {
            if (currentAC < deltaTemp)
                currentAC++;
            else if (currentAC > deltaTemp)
                currentAC--;
        }

        if (acStat.isStatOpen()) {
            int ac = MathHelper.clamp(currentAC, -MAX_AC, MAX_AC);
            String bar = (ac < 0 ? TextFormatting.BLUE : TextFormatting.GOLD)
                    + Strings.repeat("|", Math.abs(ac))
                    + TextFormatting.DARK_GRAY
                    + Strings.repeat("|", MAX_AC - Math.abs(ac));
            acStat.setTitle(new StringTextComponent("A/C: " + bar).mergeStyle(TextFormatting.YELLOW));
            acStat.setBackgroundColor(ac < 0 ? 0x300080FF : (ac == 0 ? 0x3000AA00 : 0x30FFD000));
        }
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (acStat == null) {
            acStat = new WidgetAnimatedStat(null, StringTextComponent.EMPTY, WidgetAnimatedStat.StatIcon.NONE,
                    0x3000AA00, null, ArmorHUDLayout.INSTANCE.airConStat);
            acStat.setMinimumContractedDimensions(0, 0);
        }
        return acStat;
    }

    @Override
    public void onResolutionChanged() {
        acStat = null;
    }
}
