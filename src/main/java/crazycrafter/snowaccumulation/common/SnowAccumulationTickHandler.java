package crazycrafter.snowaccumulation.common;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.BlockSnowLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class SnowAccumulationTickHandler {
	
	int randLCG;
	
	public SnowAccumulationTickHandler(){
		randLCG = (new Random()).nextInt();
	}
	
	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent e){
	    if(e.world instanceof WorldServer)
	    {
	    	WorldServer worldserver = (WorldServer) e.world;
	    	//Debug profiling for performance
	    	worldserver.profiler.startSection("AccumulationTick");
	    	if(worldserver.isRaining())
	    	{	
	    		for (Iterator<Chunk> iterator = worldserver.getPlayerChunkMap().getChunkIterator(); iterator.hasNext();)
	    		{
	    			Chunk chunk = iterator.next();
	    			int chunk_min_x = chunk.x * 16;
	    			int chunk_min_y = chunk.z * 16;
	    			//If it can rain here, there is a 1/16 chance of trying to add snow
	    			if (worldserver.dimension.canDoRainSnowIce(chunk) && worldserver.rand.nextInt(16) == 0 )
	    			{
	    				randLCG = randLCG * 3 + 1013904223;
	    				int j2 = randLCG >> 2;
	    				//Get rain height at random position in chunk, splits the random val j2 to use for both parts of position
	    				BlockPos pos1 = worldserver.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(chunk_min_x + (j2 & 15), 0, chunk_min_y + (j2 >> 8 & 15)));
	    				//Check if block at positioin is a snow layer block
	    				if(worldserver.getBlockState(pos1).getBlock() instanceof BlockSnowLayer)
	    			    {
	    					//Check if valid Y, correct light, and correct temp for snow formation
	    			    	if(pos1.getY() >= 0 && pos1.getY() < 256 && worldserver.getLightFor(EnumLightType.BLOCK, pos1 ) < 10 &&worldserver.getBiome(pos1).getTemperature(pos1) < 0.15F)
	    			    	{
	    			    		//Calculate mean surrounding block height
	    			    		int height = worldserver.getBlockState(pos1).get(BlockSnowLayer.LAYERS);
	    			    		if(height == 8) return;
	    			    		IBlockState north = worldserver.getBlockState(pos1.north());
	    			    		IBlockState south = worldserver.getBlockState(pos1.south());
	    			    		IBlockState east = worldserver.getBlockState(pos1.east());
	    			    		IBlockState west = worldserver.getBlockState(pos1.west());
	    			    		float surroundings = 0;
	    			    		if(north.getBlock() instanceof BlockSnowLayer)
	    			    		{
	    			    			surroundings += north.get(BlockSnowLayer.LAYERS);
	    			    		}else if(north.isFullCube())
	    			    		{
	    			    			surroundings += 8;
	    			    		}
	    			    		if(south.getBlock() instanceof BlockSnowLayer)
	    			   			{
	    			   				surroundings += south.get(BlockSnowLayer.LAYERS);
	    			   			}else if(south.isFullCube())
	    			   			{
	    		    				surroundings += 8;
	    		    			}
	    		    			if(east.getBlock() instanceof BlockSnowLayer)
	    		    			{
	    		    				surroundings += east.get(BlockSnowLayer.LAYERS);
	    		    			}else if(east.isFullCube())
	    		    			{
	    		    				surroundings += 8;
	    		    			}
	    		    			if(west.getBlock() instanceof BlockSnowLayer)
	    		    			{
	    		    				surroundings += west.get(BlockSnowLayer.LAYERS);
	    		    			}else if(west.isFullCube())
	    		    			{
	    		    				surroundings += 8;
	    		    			}
	    		    			surroundings /= 4;
	    		    			//Done calculating surroundings
	    		    			if(surroundings >= height)
	   			   				{
	   			   					float weight = (surroundings - height) / 2 + 0.05f;
	   			   					if(worldserver.rand.nextFloat() <= weight)
	    			   				{
	   			   						//Add layer!
	    		    					worldserver.setBlockState(pos1, Blocks.SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, height + 1));
	    		    				}
	    		    			}	
	    		    		}
	   			    	}
	   			    }
	   			}
	   		}
	    	worldserver.profiler.endSection();
	    }
	}
}
