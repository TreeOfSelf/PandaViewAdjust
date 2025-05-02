package me.TreeOfSelf;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaViewAdjust implements ModInitializer {
	public static final String MOD_ID = "panda-view-adjust";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private PandaViewConfig.ConfigEntry lastEntry = null;
	private long lastChangeTick = 0;
	private PandaViewConfig config;
	@Override
	public void onInitialize() {
		LOGGER.info("PandaViewAdjust Started!");
		config = new PandaViewConfig();

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(this::checkServerStats);

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20  == 0) {
				checkServerStats(server);
			}
		});
	}

	private void checkServerStats(MinecraftServer server) {
		int playerCount = server.getPlayerManager().getPlayerList().size();
		double mspt = server.getAverageTickTime();
		long currentTick = server.getTicks();

		PandaViewConfig.ConfigEntry bestEntry = getConfigEntry(playerCount, mspt);
		if (bestEntry == null) return;

		// Only allow changes every 10 seconds (200 ticks)
		if (currentTick - lastChangeTick < 200 /* * 6*/) return;

		if (lastEntry != null && bestEntry == lastEntry) return;

		if (lastEntry != null) {
			// Dynamic buffer based on MSPT difference
			double msptGap = Math.abs(bestEntry.maxMSPT - lastEntry.maxMSPT);
			double dynamicBuffer = Math.max(msptGap * 0.5, 2.0); // at least 2.0

			boolean msptStillWithinBuffer = mspt <= lastEntry.maxMSPT + dynamicBuffer;
			boolean playersStillOkay = playerCount <= lastEntry.maxPlayerCount;

			// Logging MSPT and dynamic buffer check
			LOGGER.info("Current MSPT: {}", mspt);
			LOGGER.info("Last maxMSPT: {}", lastEntry.maxMSPT);
			LOGGER.info("Dynamic buffer: {}", dynamicBuffer);

			if (msptStillWithinBuffer && playersStillOkay) {
				// Don't switch yet
				return;
			}
		}

		// Apply new settings
		LOGGER.info("Applying new view settings: viewDistance={}, simulationDistance={}", bestEntry.viewDistance, bestEntry.simulationDistance);
		server.getPlayerManager().setSimulationDistance(bestEntry.simulationDistance);
		server.getPlayerManager().setViewDistance(bestEntry.viewDistance);
		lastEntry = bestEntry;
		lastChangeTick = currentTick;
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
