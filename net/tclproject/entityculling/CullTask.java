package net.tclproject.entityculling;

import java.util.*;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.tclproject.entityculling.handlers.Config;
import net.tclproject.entityculling.handlers.CullableEntityRegistry;
import net.tclproject.entityculling.handlers.CullableEntityWrapper;

public class CullTask implements Runnable {

	public boolean requestCull = false;

	private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getMinecraft();
	private final int sleepDelay = Config.sleepDelay;
	private final int hitboxLimit = Config.hitboxLimit;
	private final ArrayList<String> unCullable;
	public long lastTime = 0;
	
	// reused preallocated vars
	private Vec3d lastPos = new Vec3d(0, 0, 0);
	private Vec3d aabbMin = new Vec3d(0, 0, 0);
	private Vec3d aabbMax = new Vec3d(0, 0, 0);

	public CullTask(OcclusionCullingInstance culling, String[] unCullable) {
		this.culling = culling;
		this.unCullable = new ArrayList<String>(Arrays.asList(unCullable));;
	}
	
	@Override
	public void run() {
		while (client != null) { // not correct, but the running field is hidden
			try {
				Thread.sleep(sleepDelay);

				if (EntityCullingBase.enabled && client.theWorld != null && client.thePlayer != null && client.thePlayer.ticksExisted > 10 && client.renderViewEntity != null) {
				    Vec3 cameraMC = null;
				    if(Config.debugMode) {
				        cameraMC = getPositionEyes(client.thePlayer, 0);
				    } else {
			            cameraMC = getCameraPos();
				    }
					if (requestCull || !(cameraMC.xCoord == lastPos.x && cameraMC.yCoord == lastPos.y && cameraMC.zCoord == lastPos.z)) {
						long start = System.currentTimeMillis();
						requestCull = false;
						lastPos.set(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord);
						Vec3d camera = lastPos;
						culling.resetCache();
						boolean noCulling = client.thePlayer.noClip || client.gameSettings.thirdPersonView != 0; // noClip is a 'spectator' check replacer (EtFuturum Requiem compat)
						Iterator<TileEntity> iterator = client.theWorld.loadedTileEntityList.iterator();
						TileEntity entry;
						while(iterator.hasNext()) {
							try {
								entry = iterator.next();
							}catch(NullPointerException  ex) {
								break; 
							}
							catch(ConcurrentModificationException ex) {
								break; 
							}
							if(unCullable.contains(entry.getBlockType().getUnlocalizedName())) {
								continue;
							}
							CullableEntityWrapper cullable = CullableEntityRegistry.getWrapper(entry);
							if (!cullable.isForcedVisible()) {
								if (noCulling) {
									cullable.setCulled(false);
									continue;
								}

								if(entry.getDistanceFrom(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord) < 64*64) { // 64 is the fixed max tile view distance
									AxisAlignedBB boundingBox = entry.getRenderBoundingBox();
//									aabbMin.set(entry.xCoord, entry.yCoord, entry.zCoord); // to account for larger-than-1-block TEs. possibly undo if this has unintended consequences
//								    aabbMax.set(entry.xCoord+1d, entry.yCoord+1d, entry.zCoord+1d);
									if (setBoxAndCheckLimits(cullable, boundingBox)) continue;
									if (Config.debugMode) {
										System.out.println("Currently processing tileentity " + entry.getBlockType().getUnlocalizedName());
									}
									boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
//									System.out.println(visible + "," + entry.getBlockType().getUnlocalizedName());
									cullable.setCulled(!visible);
								}

							}
						}
						Entity entity = null;
						Iterator<Entity> iterable = client.theWorld.getLoadedEntityList().iterator();
						while (iterable.hasNext()) {
							try {
								entity = iterable.next();
							}catch(NullPointerException  ex) {
								break; 
							}
							catch(ConcurrentModificationException ex) {
								break; 
							}
							if(entity == null) {
							    continue; // Not sure how this could happen
							}
							CullableEntityWrapper cullable = CullableEntityRegistry.getWrapper(entity);
							if (!cullable.isForcedVisible()) {
								if (noCulling) {
									cullable.setCulled(false);
									continue;
								}
							    if(getPositionVector(entity).squareDistanceTo(cameraMC) > Config.tracingDistance * Config.tracingDistance) {
							        cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just render it
							        continue;
							    }
							    AxisAlignedBB boundingBox = entity.boundingBox;
								if (setBoxAndCheckLimits(cullable, boundingBox)) continue;
								if (Config.debugMode) {
									System.out.println("Currently processing entity " + entity.getEntityName());
								}
								boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
								cullable.setCulled(!visible);
							}
						}
						lastTime = (System.currentTimeMillis()-start);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down culling task!");
	}

	private boolean setBoxAndCheckLimits(CullableEntityWrapper cullable, AxisAlignedBB boundingBox) {
		if(boundingBox.maxX - boundingBox.minX > hitboxLimit || boundingBox.maxY - boundingBox.minY > hitboxLimit || boundingBox.maxZ - boundingBox.minZ > hitboxLimit) {
			cullable.setCulled(false); // To big to bother to cull
			return true;
		}
		aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		return false;
	}

	public static Vec3 getPositionVector(Entity e)
	{
		return Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
	}

	@SideOnly(Side.CLIENT)
	public Vec3 getPositionEyes(Entity e, float partialTicks)
	{
		if (partialTicks == 1.0F)
		{
			return Vec3.createVectorHelper(e.posX, e.posY + (double)e.getEyeHeight(), e.posZ);
		}
		else
		{
			double d0 = e.prevPosX + (e.posX - e.prevPosX) * (double)partialTicks;
			double d1 = e.prevPosY + (e.posY - e.prevPosY) * (double)partialTicks + (double)e.getEyeHeight();
			double d2 = e.prevPosZ + (e.posZ - e.prevPosZ) * (double)partialTicks;
			return Vec3.createVectorHelper(d0, d1, d2);
		}
	}

	// 1.7.x doesn't know where the heck the camera is either
	private Vec3 getCameraPos() {
		return getPositionEyes(client.renderViewEntity, 0);

	}
}
