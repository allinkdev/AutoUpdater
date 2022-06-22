package me.allinkdev.autoupdater.runnable;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.allinkdev.autoupdater.AutoUpdater;
import me.allinkdev.autoupdater.ManagedPlugin;
import me.allinkdev.autoupdater.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.request.RequestMaker;
import me.allinkdev.autoupdater.response.Release;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class UpdatePlugins implements Runnable {

	private final Logger logger = LoggerFactory.getLogger("PluginUpdater/Update Task");
	private final AutoUpdater plugin;

	@Override
	public void run() {
		final long millis = System.currentTimeMillis();
		final FileConfiguration configuration = plugin.getConfig();
		final Map<String, String> lastDownloadedVersions = plugin.getLastDownloadedVersions();

		configuration.set("lastCheckedTimestamp", millis);

		final List<ManagedPlugin> managedPlugins = plugin.getManagedPlugins();

		for (ManagedPlugin managedPlugin : managedPlugins) {
			final ArtifactIdentity identity = managedPlugin.getArtifactIdentity();
			final String stringifiedIdentity = identity.get();
			final RequestMaker requestMaker = managedPlugin.getRequestMaker();
			final Release latestRelease;

			try {
				latestRelease = requestMaker.getLatestRelease();
			} catch (Exception e) {
				throw new RuntimeException("Error checking release information for %s.".formatted(identity), e);
			}

			final String releaseTag = latestRelease.getName();
			final String lastDownloadedTag = lastDownloadedVersions.getOrDefault(identity.getArtifactName(), "");

			if (lastDownloadedTag.equalsIgnoreCase(releaseTag)) {
				logger.info("Already downloaded latest release of {} (remote: {}, server: {})!",
					stringifiedIdentity,
					releaseTag, lastDownloadedTag);
				continue;
			}

			logger.info("Downloading new release of {} (remote has version {} while server has {})...",
				stringifiedIdentity, releaseTag,
				lastDownloadedTag);

			try {
				requestMaker.downloadRelease(latestRelease);
			} catch (Exception e) {
				logger.warn("Exception while trying to download plugin {}!", stringifiedIdentity, e);
			}

			lastDownloadedVersions.put(identity.getArtifactName(), releaseTag);
			plugin.saveLastDownloadedVersions(lastDownloadedVersions);
		}
	}
}
