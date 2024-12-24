package com.logisticscraft.occlusionculling;

import com.logisticscraft.occlusionculling.util.Vec3d;

public interface DataProvider {

    /**
     * Prepares the requested chunk. Returns true if the chunk is ready, false when
     * not loaded. Should not reload the chunk when the x and y are the same as the
     * last request!
     *
     * @param chunkX
     * @param chunkZ
     * @return true if the chunk is ready, false otherwise
     */
    boolean prepareChunk(int chunkX, int chunkZ);

    /**
     * Location is inside the chunk.
     *
     * @param x
     * @param y
     * @param z
     * @return true if the location is an opaque full cube, false otherwise
     */
    boolean isOpaqueFullCube(int x, int y, int z);

    // These methods are no longer default; they must be implemented or provided in a base class
    void cleanup();

    void checkingPosition(Vec3d[] targetPoints, int size, Vec3d viewerPosition);
}
