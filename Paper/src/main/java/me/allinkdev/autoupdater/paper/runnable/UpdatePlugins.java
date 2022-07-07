package me.allinkdev.autoupdater.paper.runnable;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.allinkdev.autoupdater.common.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.common.request.RequestMaker;
import me.allinkdev.autoupdater.common.response.Release;
import me.allinkdev.autoupdater.common.runnable.UpdateTask;
import me.allinkdev.autoupdater.paper.Main;
import me.allinkdev.autoupdater.paper.ManagedPlugin;
import org.bukkit.configuration.file.FileConfiguration;

@AllArgsConstructor
public class UpdatePlugins extends UpdateTask {

	private final Main plugin;

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
				throw new RuntimeException(
					"Error checking release information for %s.".formatted(identity), e);
			}

			final String releaseTag = latestRelease.getName();
			final String lastDownloadedTag = lastDownloadedVersions.getOrDefault(
				identity.getArtifactName(), "");

			if (lastDownloadedTag.equalsIgnoreCase(releaseTag)) {
				logger.info("Already downloaded latest release of {} (remote: {}, server: {})!",
					stringifiedIdentity,
					releaseTag, lastDownloadedTag);
				continue;
			}

			logger.info(
				"Downloading new release of {} (remote has version {} while server has {})...",
				stringifiedIdentity, releaseTag,
				lastDownloadedTag);

			try {
				requestMaker.downloadRelease(latestRelease, plugin.getPluginDirectory());
			} catch (Exception e) {
				logger.warn("Exception while trying to download plugin {}!", stringifiedIdentity,
					e);
			}

			lastDownloadedVersions.put(identity.getArtifactName(), releaseTag);
			plugin.saveLastDownloadedVersions(lastDownloadedVersions);
		}
	}
}
