package net.tclproject.entityculling;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import com.logisticscraft.occlusionculling.cache.ArrayOcclusionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.tclproject.entityculling.handlers.Config;

public abstract class EntityCullingBase {

    public OcclusionCullingInstance culling;
    public boolean debugHitboxes = false;
    public static boolean enabled = true; // public static to make it faster for the jvm
    public CullTask cullTask;
    private Thread cullThread;
    protected KeyBinding keybind = new KeyBinding("Entity Culling Key", 19);
    protected boolean pressed = false;

    //stats
    public int renderedBlockEntities = 0;
    public int skippedBlockEntities = 0;
    public int renderedEntities = 0;
    public int skippedEntities = 0;
    //public int tickedEntities = 0;
    //public int skippedEntityTicks = 0;

    public void onInitialize() {
        if(Config.aggressiveMode) {
            culling = new OcclusionCullingInstance(Config.tracingDistance, new Provider(), new ArrayOcclusionCache(Config.tracingDistance), 0);
        } else {
            culling = new OcclusionCullingInstance(Config.tracingDistance, new Provider());
        }
        cullTask = new CullTask(culling, Config.blockEntityWhitelist);

        cullThread = new Thread(cullTask, "CullThread");
        cullThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
			    System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
			    ex.printStackTrace();
			}
		});
        cullThread.start();
        initModloader();
    }

    public void worldTick() {
        cullTask.requestCull = true;
    }

    public void clientTick() {
        if (keybind.isPressed()) {
            if (pressed)
                return;
            pressed = true;
            enabled = !enabled;
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            if(enabled) {
                if (player != null) {
                    //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
                    player.addChatMessage("Culling on");
                    EntityCulling.instance.renderedBlockEntities = 0;
                    EntityCulling.instance.skippedBlockEntities = 0;
                    EntityCulling.instance.renderedEntities = 0;
                    EntityCulling.instance.skippedEntities = 0;
                }
            } else {
                if (player != null) {
                	player.addChatMessage("[Culling] Last pass: " + EntityCulling.instance.cullTask.lastTime + "ms");
                    player.addChatMessage("[Culling] Rendered Block Entities: " + EntityCulling.instance.renderedBlockEntities + " Skipped: " + EntityCulling.instance.skippedBlockEntities);
                    player.addChatMessage("[Culling] Rendered Entities: " + EntityCulling.instance.renderedEntities + " Skipped: "+ EntityCulling.instance.skippedEntities); 
                    player.addChatMessage("Culling off");
                }
            }
        } else {
            pressed = false;
        }
        cullTask.requestCull = true;
    }

    public abstract void initModloader();

}
