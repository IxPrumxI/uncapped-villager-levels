package me.prum.uncappedvillagerlevels;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("uncapped-villager-levels");
	@Override
	public void onInitialize() {
		LOGGER.info("Uncapped Villager Levels is initializing.");
	}
}