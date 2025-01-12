package net.tclproject.entityculling.handlers;

import com.google.common.collect.MapMaker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class CullableEntityRegistry {
//    public static Map<Entity, CullableEntityWrapper> entityWrappers = new HashMap<>();
//    public static Map<TileEntity, CullableEntityWrapper> tileWrappers = new HashMap<>();
    private static ConcurrentMap<TileEntity, CullableEntityWrapper> tileWrappers = new MapMaker().weakKeys().concurrencyLevel(3).makeMap();
    private static ConcurrentMap<Entity, CullableEntityWrapper> entityWrappers = new MapMaker().weakKeys().concurrencyLevel(3).makeMap();

    public static CullableEntityWrapper getWrapper(Entity e) {
        if (!entityWrappers.containsKey(e)) entityWrappers.put(e, new CullableEntityWrapper(e));
        return entityWrappers.get(e);
    }

    public static CullableEntityWrapper getWrapper(TileEntity e) {
        if (!tileWrappers.containsKey(e)) tileWrappers.put(e, new CullableEntityWrapper(e));
        return tileWrappers.get(e);
    }

   public static void cleanupWrappers() { // test if weak keys don't work properly
        entityWrappers = new MapMaker().weakKeys().concurrencyLevel(3).makeMap();
        tileWrappers = new MapMaker().weakKeys().concurrencyLevel(3).makeMap();
   }
}
