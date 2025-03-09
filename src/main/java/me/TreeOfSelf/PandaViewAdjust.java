package me.TreeOfSelf;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaViewAdjust implements ModInitializer {
	public static final String MOD_ID = "panda-view-adjust";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private PandaViewConfig config;

	@Override
	public void onInitialize() {
		LOGGER.info("PandaViewAdjust Started!");
		config = new PandaViewConfig();

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) {
				checkServerStats(server);
			}
		});
	}

	private void checkServerStats(MinecraftServer server) {
		int playerCount = server.getPlayerManager().getPlayerList().size();
		double mspt = server.getAverageTickTime();
		PandaViewConfig.ConfigEntry bestEntry = getConfigEntry(playerCount, mspt);
		server.getPlayerManager().setSimulationDistance(bestEntry.simulationDistance);
		server.getPlayerManager().setViewDistance(bestEntry.viewDistance);

	}

	private PandaViewConfig.@Nullable ConfigEntry getConfigEntry(int playerCount, double mspt) {
		PandaViewConfig.ConfigEntry bestEntry = null;

		for (PandaViewConfig.ConfigEntry entry : config.getConfigEntries()) {
			boolean isPlayerCountValid = (entry.maxPlayerCount == 0 || playerCount <= entry.maxPlayerCount);
			boolean isMSPTValid = (entry.maxMSPT == 0 || mspt <= entry.maxMSPT);

			if (isPlayerCountValid && isMSPTValid) {
				if (bestEntry == null ||
						(entry.maxPlayerCount != 0 && entry.maxPlayerCount < bestEntry.maxPlayerCount) ||
						(entry.maxMSPT != 0 && entry.maxMSPT < bestEntry.maxMSPT)) {
					bestEntry = entry;
				}
			}
		}
		return bestEntry;
	}
}
