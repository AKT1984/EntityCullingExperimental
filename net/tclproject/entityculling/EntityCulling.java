package net.tclproject.entityculling;

import java.util.Map;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.tclproject.entityculling.handlers.Config;

@Mod(modid = EntityCulling.MODID, version = EntityCulling.VERSION, name = EntityCulling.NAME)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
@IFMLLoadingPlugin.TransformerExclusions({"net.tclproject.entityculling.coremod"})
public class EntityCulling extends EntityCullingBase{
    public static final String MODID = "entityculling";
    public static final String NAME = "Entity Culling Unofficial";
    public static final String VERSION = "1.0.1";

    @Mod.Instance("entityculling")
    public static EntityCulling instance;

    private boolean onServer = false;
    
    
    
    
    
    
    @Override
    public void initModloader() {

    }


	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Config.load(event);

        
    }


    
    
    @EventHandler
	public void PostInit(FMLPostInitializationEvent event) {
    	if (onServer) return;
    	 KeyBindingRegistry.registerKeyBinding(new EntityCullingKeyHandler(keybind));
         TickRegistry.registerTickHandler(new EntityCullingTickHandler(), Side.CLIENT);
         MinecraftForge.EVENT_BUS.register(this);
         
      // Check if running on a client
         try {
             Class<?> clientClass = net.minecraft.client.Minecraft.class;
         } catch (Throwable ex) {
             System.out.println("EntityCulling Mod installed on a Server. Going to sleep.");
             onServer = true;
             return;
         }
         

         System.out.println("EntityCulling Mod installed on a Client.");
         onInitialize();
        


    }
    
    
    
    



}
