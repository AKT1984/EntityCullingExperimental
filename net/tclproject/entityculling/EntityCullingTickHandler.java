package net.tclproject.entityculling;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.tclproject.entityculling.handlers.CullableEntityRegistry;

import java.util.EnumSet;
import java.util.Map;

public class EntityCullingTickHandler implements ITickHandler {

    private boolean appliedWrappers = false;
    private boolean postInitCompleted = false;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        if (type.contains(TickType.CLIENT)) {
            EntityCulling.instance.clientTick(); // Call client tick logic

            // Wait until post-initialization is completed
            if (!postInitCompleted && Minecraft.getMinecraft().theWorld != null) {
                postInitCompleted = true; // Mark as completed when the world is initialized
            }

            // Apply wrappers after post-initialization
            if (postInitCompleted && !appliedWrappers) {
                wrapRenderers();
                appliedWrappers = true;
            }
        } else if (type.contains(TickType.WORLD)) {
            EntityCulling.instance.worldTick(); // Call world tick logic
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        // Handle end-of-tick logic if needed
    }

    @Override
    public EnumSet<TickType> ticks() {
        // Listen for both CLIENT and WORLD tick types
        return EnumSet.of(TickType.CLIENT, TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "EntityCullingTickHandler";
    }

    private void wrapRenderers() {
    	CullableEntityRegistry.cleanupWrappers();
        // Wrap entity renderers
        RenderManager renderManager = RenderManager.instance;
        Map<Class<?>, Render> entityRenderMap = renderManager.entityRenderMap;

        for (Map.Entry<Class<?>, Render> entry : entityRenderMap.entrySet()) {
            Render originalRenderer = entry.getValue();

            // Skip specialized renderers (e.g., RenderPlayer)
            if (entry.getKey() == RenderPlayer.class || originalRenderer instanceof RenderPlayer) {
                continue;
            }

            // Wrap other renderers with CullingEntityRenderer
            CullingEntityRenderer wrappedRenderer = new CullingEntityRenderer(originalRenderer);
            entry.setValue(wrappedRenderer);
        }
        System.out.println("EntityCulling: Entity enderers wrapped successfully after Forge finished loading.");

        // Wrap tile entity renderers
        Map<Class<?>, TileEntitySpecialRenderer> specialRendererMap = TileEntityRenderer.instance.specialRendererMap;

        for (Map.Entry<Class<?>, TileEntitySpecialRenderer> entry : specialRendererMap.entrySet()) {
            TileEntitySpecialRenderer originalRenderer = entry.getValue();

            // Skip null renderers
            if (originalRenderer == null) {
                continue;
            }

            // Wrap the renderer with a culling-aware version
            CullingTileEntityRenderer wrappedRenderer = new CullingTileEntityRenderer(originalRenderer);
            entry.setValue(wrappedRenderer);
        }

        System.out.println("EntityCulling: Tile-Entities renderers wrapped successfully.");
    }
}
