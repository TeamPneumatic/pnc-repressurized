package me.desht.pneumaticcraft.common.sensor.pollSensors.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class EntityInRangeSensor extends EntityPollSensor {

    private EntityFilter filter;

    @Override
    public String getSensorPath() {
        return "Within Range";
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public int getRedstoneValue(List<Entity> entities, String textboxText) {
        if (filter == null) {
            filter = new EntityFilter(textboxText);
        }

        int entitiesFound = textboxText.isEmpty() ?
                entities.size() :
                (int) entities.stream().filter(entity -> filter.test(entity)).count();
        return Math.min(15, entitiesFound);
    }

    @Override
    public Class<? extends Entity> getEntityTracked() {
        return Entity.class;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
        fontRenderer.drawString(matrixStack, I18n.format("pneumaticcraft.gui.entityFilter"), 70, 48, 0x404040);
    }

    @Override
    public void notifyTextChange(String newText) {
        filter = new EntityFilter(newText);
    }
}
