package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPressurizedSpawner;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressurizedSpawner;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPressurizedSpawner extends GuiPneumaticContainerBase<ContainerPressurizedSpawner, TileEntityPressurizedSpawner> {
    WidgetAnimatedStat infoStat;

    public GuiPressurizedSpawner(ContainerPressurizedSpawner container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetButtonExtended(guiLeft + 152, guiTop + 66, 16, 16, "R", b -> { closeScreen(); te.showRangeLines(); }));

        infoStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.PRESSURIZED_SPAWNER.get()), 0xFF4E4066, false);
    }

    @Override
    public void tick() {
        super.tick();

        int spawnRate = te.getSpawnInterval();
        int airUsage = (int) (PneumaticValues.USAGE_PRESSURIZED_SPAWNER * te.getSpeedUsageMultiplierFromUpgrades());
        infoStat.setText(ImmutableList.of(
            I18n.format("pneumaticcraft.gui.tab.status.pressurizedSpawner.spawnRate", spawnRate),
            I18n.format("pneumaticcraft.gui.tab.status.pressurizedSpawner.airUsage", airUsage)
        ));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_TRAP;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.NO_CORE) {
            curInfo.add(I18n.format(te.problem.getTranslationKey()));
        }
    }
}
