package net.tclproject.entityculling;

import java.lang.reflect.Method;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.tclproject.entityculling.handlers.CullableEntityRegistry;
import net.tclproject.entityculling.handlers.CullableEntityWrapper;

public class CullingEntityRenderer extends Render {

    private final Render originalRenderer;
    private final Method getEntityTextureMethod;

    public CullingEntityRenderer(Render originalRenderer) {
        this.originalRenderer = originalRenderer;

        // Obtain access to the protected method using reflection
        Method method = null;
        try {
            method = Render.class.getDeclaredMethod("getEntityTexture", Entity.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.getEntityTextureMethod = method;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        CullableEntityWrapper cullable = CullableEntityRegistry.getWrapper(entity);
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
        	EntityCulling.instance.skippedEntities++;
            return; // Skip rendering
        }
        EntityCulling.instance.renderedEntities++;
        originalRenderer.doRender(entity, x, y, z, yaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        try {
            // Use reflection to invoke the protected method on the original renderer
            return (ResourceLocation) getEntityTextureMethod.invoke(originalRenderer, entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return a fallback if reflection fails
        }
    }
    public void doRenderShadowAndFire(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
    	CullableEntityWrapper cullable = CullableEntityRegistry.getWrapper(par1Entity);
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            return; // Skip rendering
        }
        originalRenderer.doRenderShadowAndFire(par1Entity, par2, par4, par6, par8, par9);
    }
}
