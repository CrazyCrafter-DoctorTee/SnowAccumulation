package com.crazycrafter.snowaccumulation.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = SnowAccumulation.MODID, version = SnowAccumulation.VERSION)
public class SnowAccumulation
{

    public static final String MODID = "snowaccumulation";
    public static final String VERSION = "1.0.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new SnowAccumulationTickHandler());
    }
}
