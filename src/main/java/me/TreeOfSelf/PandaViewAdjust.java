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
	private PandaViewConfig.ConfigEntry pendingEntry = null;
	private long lastChangeTick = 0;
	private PandaViewConfig config;
	// Counter to track consecutive matches of the same pending entry
	private int pendingMatchCount = 0;
	// Required consecutive matches before applying change
	private static final int REQUIRED_MATCHES = 6;

	@Override
	public void onInitialize() {
		LOGGER.info("PandaViewAdjust Started!");
		config = new PandaViewConfig();

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(this::checkServerStats);

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) {
				checkServerStats(server);
			}
		});
	}

	private void checkServerStats(MinecraftServer server) {
		int playerCount = server.getPlayerManager().getPlayerList().size();
		double mspt = server.getAverageTickTime();
		long currentTick = server.getTicks();

		PandaViewConfig.ConfigEntry candidate = getConfigEntry(playerCount, mspt);
		if (candidate == null) return;

		// Wait at least 200 ticks before any change
		if (currentTick - lastChangeTick < 200) return;

		// If already using this config, no need to change
		if (lastEntry != null && candidate == lastEntry) {
			pendingEntry = null; // reset
			pendingMatchCount = 0; // reset counter
			return;
		}

		// First time selecting a new candidate or different from current pending
		if (pendingEntry == null || pendingEntry != candidate) {
			// Reset counter and set new pending entry
			pendingMatchCount = 1; // First match
			pendingEntry = candidate;
			LOGGER.info("Pending view setting change to: viewDistance={}, simulationDistance={} (1/{} matches)",
					pendingEntry.viewDistance, pendingEntry.simulationDistance, REQUIRED_MATCHES);
			return;
		}

		// Consecutive match for the same pending entry
		pendingMatchCount++;
		LOGGER.info("Pending view setting match: viewDistance={}, simulationDistance={} ({}/{} matches)",
				pendingEntry.viewDistance, pendingEntry.simulationDistance, pendingMatchCount, REQUIRED_MATCHES);

		// Only apply after reaching required consecutive matches
		if (pendingMatchCount >= REQUIRED_MATCHES) {
			LOGGER.info("Applying new view settings: viewDistance={}, simulationDistance={}",
					pendingEntry.viewDistance, pendingEntry.simulationDistance);
			server.getPlayerManager().setSimulationDistance(pendingEntry.simulationDistance);
			server.getPlayerManager().setViewDistance(pendingEntry.viewDistance);

			lastEntry = pendingEntry;
			pendingEntry = null;
			pendingMatchCount = 0;
			lastChangeTick = currentTick;
		}
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