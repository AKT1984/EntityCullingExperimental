package net.tclproject.entityculling;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.tclproject.entityculling.handlers.CullableEntityRegistry;
import net.tclproject.entityculling.handlers.CullableEntityWrapper;

public class CullingTileEntityRenderer extends TileEntitySpecialRenderer {

    private final TileEntitySpecialRenderer originalRenderer;

    public CullingTileEntityRenderer(TileEntitySpecialRenderer originalRenderer) {
        this.originalRenderer = originalRenderer;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        CullableEntityWrapper cullable = CullableEntityRegistry.getWrapper(tileEntity);
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
        	EntityCulling.instance.skippedBlockEntities++;
            return; // Skip rendering
        }
        EntityCulling.instance.renderedBlockEntities++;
        originalRenderer.renderTileEntityAt(tileEntity, x, y, z, partialTicks);
    }
}
