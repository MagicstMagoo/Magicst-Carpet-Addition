package cn.sculk.magicst;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class main implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("WNS");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Minecraft");
	}
}
