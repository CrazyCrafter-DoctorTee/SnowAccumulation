package com.crazycrafter.snowaccumulation.common;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class SnowAccumulationTickHandler {
	
	int randLCG = (new Random()).nextInt();
	
	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent e){
	    if(e.world instanceof WorldServer)
	    {
	    	WorldServer worldserver = (WorldServer) e.world;
	    	if(worldserver.isRaining())
	    	{	
	    		for (Iterator<Chunk> iterator = net.minecraftforge.common.ForgeChunkManager.getPersistentChunksIterableFor(worldserver, worldserver.getPlayerChunkMap().getChunkIterator()); iterator.hasNext();)
	    		{
	    			Chunk chunk = iterator.next();
	    			int j = chunk.x * 16;
	    			int k = chunk.z * 16;;
	    			if (worldserver.provider.canDoRainSnowIce(chunk) && worldserver.rand.nextInt(16) == 0 )
	    			{
	    				randLCG = randLCG * 3 + 1013904223;
	    				int j2 = randLCG >> 2;
	    				BlockPos pos1 = worldserver.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
	    				if(worldserver.getBlockState(pos1).getBlock() instanceof BlockSnow)
	    			    {
	    			    	if(pos1.getY() >= 0 && pos1.getY() < 256 && worldserver.getLightFor(EnumSkyBlock.BLOCK, pos1 ) < 10 &&worldserver.getBiome(pos1).getFloatTemperature(pos1) < 0.15F)
	    			    	{
	    			    		int height = worldserver.getBlockState(pos1).getValue(BlockSnow.LAYERS);
	    			    		if(height == 8) return;
	    			    		IBlockState north = worldserver.getBlockState(pos1.north());
	    			    		IBlockState south = worldserver.getBlockState(pos1.south());
	    			    		IBlockState east = worldserver.getBlockState(pos1.east());
	    			    		IBlockState west = worldserver.getBlockState(pos1.west());
	    			    		float surroundings = 0;
	    			    		if(north.getBlock() instanceof BlockSnow)
	    			    		{
	    			    			surroundings += north.getValue(BlockSnow.LAYERS);
	    			    		}else if(north.isFullBlock())
	    			    		{
	    			    			surroundings += 8;
	    			    		}
	    			    		if(south.getBlock() instanceof BlockSnow)
	    			   			{
	    			   				surroundings += south.getValue(BlockSnow.LAYERS);
	    			   			}else if(south.isFullBlock())
	    			   			{
	    		    				surroundings += 8;
	    		    			}
	    		    			if(east.getBlock() instanceof BlockSnow)
	    		    			{
	    		    				surroundings += east.getValue(BlockSnow.LAYERS);
	    		    			}else if(east.isFullBlock())
	    		    			{
	    		    				surroundings += 8;
	    		    			}
	    		    			if(west.getBlock() instanceof BlockSnow)
	    		    			{
	    		    				surroundings += west.getValue(BlockSnow.LAYERS);
	    		    			}else if(west.isFullBlock())
	    		    			{
	    		    				surroundings += 8;
	    		    			}
	    		    			surroundings /= 4;
	    		    			if(surroundings >= height)
	   			   				{
	   			   					float weight = (surroundings - height) / 2 + 0.05f;
	   			   					if(worldserver.rand.nextFloat() <= weight)
	    			   				{
	    		    					worldserver.setBlockState(pos1, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height + 1));
	    		    				}
	    		    			}	
	    		    		}
	   			    	}
	   			    }
	   			}
	   		}
	    }
	}
}
