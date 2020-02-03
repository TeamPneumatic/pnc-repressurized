package me.desht.pneumaticcraft.common.progwidgets.area;

import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeCylinder extends AreaType {

    public static final String ID = "cylinder";

    private EnumAxis axis = EnumAxis.X;
    private EnumCylinderType cylinderType = EnumCylinderType.FILLED;

    private enum EnumCylinderType {
        FILLED("filled"), HOLLOW("hollow"), TUBE("tube");

        private final String name;

        EnumCylinderType(String name) {
            this.name = "gui.progWidget.area.type.cylinder.cylinderType." + name;
        }

        @Override
        public String toString() {
            return I18n.format(name);
        }
    }

    public AreaTypeCylinder() {
        super(ID);
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        switch (axis) {
            case X: {
                double rad = PneumaticCraftUtils.distBetween(p1.getY(), p1.getZ(), p2.getY(), p2.getZ());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minY = (int) (p1.getY() - rad - 1);
                minZ = (int) (p1.getZ() - rad - 1);
                maxY = (int) (p1.getY() + rad + 1);
                maxZ = (int) (p1.getZ() + rad + 1);
                for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getY(), p1.getZ(), y, z);
                        if (centerDistSq <= radSq) {
                            for (int x = minX; x <= maxX; x++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (x == minX || x == maxX)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            break;
            case Y: {
                double rad = PneumaticCraftUtils.distBetween(p1.getX(), p1.getZ(), p2.getX(), p2.getZ());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minX = (int) (p1.getX() - rad - 1);
                minZ = (int) (p1.getZ() - rad - 1);
                maxX = (int) (p1.getX() + rad + 1);
                maxZ = (int) (p1.getZ() + rad + 1);
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getX(), p1.getZ(), x, z);
                        if (centerDistSq <= radSq) {
                            for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (y == minY || y == maxY)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            break;
            case Z: {
                double rad = PneumaticCraftUtils.distBetween(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minX = (int) (p1.getX() - rad - 1);
                minY = (int) (p1.getY() - rad - 1);
                maxX = (int) (p1.getX() + rad + 1);
                maxY = (int) (p1.getY() + rad + 1);
                for (int x = minX; x <= maxX; x++) {
                    for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getX(), p1.getY(), x, y);
                        if (centerDistSq <= radSq) {
                            for (int z = minZ; z <= maxZ; z++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (z == minZ || z == maxZ)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            break;
            default:
                throw new IllegalArgumentException(axis.toString());
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidgetEnum<>("gui.progWidget.area.type.cylinder.cylinderType", EnumCylinderType.class, () -> cylinderType, cylinderType -> this.cylinderType = cylinderType));
        widgets.add(new AreaTypeWidgetEnum<>("gui.progWidget.area.type.general.axis", EnumAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putByte("axis", (byte) axis.ordinal());
        tag.putByte("cylinderType", (byte) cylinderType.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        axis = EnumAxis.values()[tag.getByte("axis")];
        cylinderType = EnumCylinderType.values()[tag.getByte("cylinderType")];
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeByte(axis.ordinal());
        buffer.writeByte(cylinderType.ordinal());
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        axis = EnumAxis.values()[buf.readByte()];
        cylinderType = EnumCylinderType.values()[buf.readByte()];
    }

    @Override
    public void convertFromLegacy(LegacyAreaWidgetConverter.EnumOldAreaType oldAreaType, int typeInfo) {
        switch (oldAreaType) {
            case X_CYLINDER:
                axis = EnumAxis.X;
                break;
            case Y_CYLINDER:
                axis = EnumAxis.Y;
                break;
            case Z_CYLINDER:
                axis = EnumAxis.Z;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
