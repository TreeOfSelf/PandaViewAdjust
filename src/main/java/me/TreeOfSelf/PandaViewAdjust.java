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
	private int pendingMatchCount = 0;

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

		if (currentTick - lastChangeTick < 200) return;

		if (lastEntry != null && isWithinBuffer(lastEntry, playerCount, mspt)) {
			pendingEntry = null;
			pendingMatchCount = 0;
			return;
		}

		if (lastEntry != null && candidate == lastEntry) {
			pendingEntry = null;
			pendingMatchCount = 0;
			return;
		}

		if (pendingEntry == null || pendingEntry != candidate) {
			pendingMatchCount = 1;
			pendingEntry = candidate;
			return;
		}

		pendingMatchCount++;

		if (pendingMatchCount >= config.getRequiredMatches()) {
			server.getPlayerManager().setSimulationDistance(pendingEntry.simulationDistance);
			server.getPlayerManager().setViewDistance(pendingEntry.viewDistance);

			LOGGER.info("Changed view settings to: view={}, sim={}",
					pendingEntry.viewDistance,
					pendingEntry.simulationDistance);

			lastEntry = pendingEntry;
			pendingEntry = null;
			pendingMatchCount = 0;
			lastChangeTick = currentTick;
		}
	}


	private boolean isWithinBuffer(PandaViewConfig.ConfigEntry entry, int playerCount, double mspt) {
		if (entry.maxMSPT == 0) return false;

		double bufferAmount = entry.maxMSPT * config.getMsptBufferPercentage();

		boolean msptInBuffer = mspt > entry.maxMSPT && mspt <= entry.maxMSPT + bufferAmount;

		boolean playerCountValid = (entry.maxPlayerCount == 0 || playerCount <= entry.maxPlayerCount);

		return msptInBuffer && playerCountValid;
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