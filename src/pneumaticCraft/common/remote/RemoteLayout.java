package pneumaticCraft.common.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pneumaticCraft.client.gui.widget.IGuiWidget;

public class RemoteLayout{

    private final List<ActionWidget> actionWidgets = new ArrayList<ActionWidget>();
    private static final Map<String, Class<? extends ActionWidget>> registeredWidgets = new HashMap<String, Class<? extends ActionWidget>>();

    static {
        registerWidget(ActionWidgetCheckBox.class);
        registerWidget(ActionWidgetLabel.class);
        registerWidget(ActionWidgetButton.class);
        registerWidget(ActionWidgetDropdown.class);
    }

    private static void registerWidget(Class<? extends ActionWidget> widgetClass){
        try {
            ActionWidget widget = widgetClass.newInstance();
            registeredWidgets.put(widget.getId(), widgetClass);
            return;
        } catch(InstantiationException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Widget " + widgetClass + " couldn't be registered");
    }

    public RemoteLayout(ItemStack remote, int guiLeft, int guiTop){
        NBTTagCompound tag = remote.getTagCompound();
        if(tag != null) {
            NBTTagList tagList = tag.getTagList("actionWidgets", 10);
            for(int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound widgetTag = tagList.getCompoundTagAt(i);
                String id = widgetTag.getString("id");
                Class<? extends ActionWidget> clazz = registeredWidgets.get(id);
                try {
                    ActionWidget widget = clazz.newInstance();
                    widget.readFromNBT(widgetTag, guiLeft, guiTop);
                    actionWidgets.add(widget);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public NBTTagCompound toNBT(int guiLeft, int guiTop){
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagList tagList = new NBTTagList();
        for(ActionWidget actionWidget : actionWidgets) {
            tagList.appendTag(actionWidget.toNBT(guiLeft, guiTop));
        }
        tag.setTag("actionWidgets", tagList);
        return tag;
    }

    public void addWidget(ActionWidget widget){
        actionWidgets.add(widget);
    }

    public List<ActionWidget> getActionWidgets(){
        return actionWidgets;
    }

    public List<IGuiWidget> getWidgets(boolean filterDisabledWidgets){
        List<IGuiWidget> widgets = new ArrayList<IGuiWidget>();
        for(ActionWidget actionWidget : actionWidgets) {
            if(!filterDisabledWidgets || actionWidget.isEnabled()) {
                widgets.add(actionWidget.getWidget());
            }
        }
        return widgets;
    }

}
