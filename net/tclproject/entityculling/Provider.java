package net.tclproject.entityculling;
import com.logisticscraft.occlusionculling.DataProvider;
import com.logisticscraft.occlusionculling.util.Vec3d;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getMinecraft();
    private WorldClient world = null;

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.theWorld;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        int blockId = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockId];
        return block != null && block.isOpaqueCube();
    }




    @Override
    public void cleanup() {
        world = null;
    }

	@Override
	public void checkingPosition(Vec3d[] targetPoints, int size, Vec3d viewerPosition) {
		// TODO Auto-generated method stub
		
	}

}
