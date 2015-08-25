package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class GuiConfigHandler implements IModGuiFactory{

    @Override
    public void initialize(Minecraft minecraftInstance){
        // TODO Auto-generated method stub

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass(){
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element){
        // TODO Auto-generated method stub
        return null;
    }

    public static class ConfigGui extends GuiConfig{

        public ConfigGui(GuiScreen parent){

            super(parent, getConfigElements(), Names.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(Config.config.toString()));
        }

        private static List<IConfigElement> getConfigElements(){
            List<IConfigElement> list = new ArrayList<IConfigElement>();
            for(String category : Config.CATEGORIES) {
                list.add(new DummyCategoryElement(category, category, new ConfigElement(Config.config.getCategory(category).setRequiresMcRestart(!Config.NO_MC_RESTART_CATS.contains(category))).getChildElements()));
            }

            return list;
        }
    }

}
