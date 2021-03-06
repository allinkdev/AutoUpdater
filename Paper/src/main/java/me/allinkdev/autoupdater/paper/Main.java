package me.allinkdev.autoupdater.paper;

import com.google.common.io.Resources;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import me.allinkdev.autoupdater.common.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.common.response.Release;
import me.allinkdev.autoupdater.paper.runnable.UpdatePlugins;
import me.allinkdev.autoupdater.paper.utility.PluginUtility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class Main extends JavaPlugin {

	private static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = null;
	private static Main INSTANCE;

	@Getter
	private final List<ManagedPlugin> managedPlugins = new ArrayList<>();
	private String configurationLocation;
	private Logger logger;
	private FileConfiguration configuration;
	@Getter
	private Path pluginDirectory;
	private Path dataFolder;

	public static Main getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void onLoad() {
		dataFolder = getDataFolder().toPath();
		pluginDirectory = dataFolder.resolve("subplugins");

		final File subPluginsFile = pluginDirectory.toFile();

		if (!subPluginsFile.exists()) {
			subPluginsFile.mkdirs();
		}

		final Path configurationPath = dataFolder.resolve("config.yml");
		final File configurationFile = configurationPath.toFile();

		configurationLocation = configurationPath.toString();

		if (!configurationFile.exists()) {
			try {
				final URL resourceUrl = this.getClassLoader().getResource("config.yml");

				assert resourceUrl != null;

				Resources.copy(resourceUrl,
					new FileOutputStream(configurationFile));
			} catch (IOException e) {
				throw new RuntimeException("Unable to save default configuration for plugin!", e);
			}
		}

		getConfig().options().copyDefaults(true);
		saveConfig();

		configuration = getConfig();

		INSTANCE = this;
		logger = getSLF4JLogger();
	}

	@Override
	public void onEnable() {
		buildManagedPluginsList();
		schedulePluginUpdates();
		downloadNewPlugins();
		enablePlugins();
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();

		configuration = getConfig();
	}

	public void schedulePluginUpdates() {
		final long configuredInterval = configuration.getLong("checkInterval");
		final TimeUnit configuredIntervalTimeUnit = TimeUnit.valueOf(
			configuration.getString("checkTimeUnit"));

		if (SCHEDULED_EXECUTOR_SERVICE != null) {
			SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
		}

		SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

		SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new UpdatePlugins(this), configuredInterval,
			configuredInterval,
			configuredIntervalTimeUnit);
	}

	public void buildManagedPluginsList() {
		managedPlugins.clear();

		final List<String> plugins = configuration.getStringList("plugins");

		if (plugins.size() == 0) {
			logger.warn(
				"No plugins defined! The plugin will do practically nothing if you don't define any plugins in {}!",
				configurationLocation);
			return;
		}

		for (String plugin : plugins) {
			this.processConfiguredPlugin(plugin);
		}
	}

	private void processConfiguredPlugin(String plugin) {
		logger.info("Loading configured plugin {}...", plugin);

		final ArtifactIdentity artifactIdentity = ArtifactIdentity.fromString(plugin);
		final File pluginFile = PluginUtility.getPluginFileFor(artifactIdentity);
		final ManagedPlugin managedPlugin = new ManagedPlugin(artifactIdentity, pluginFile);

		managedPlugins.add(managedPlugin);

		logger.info("Loaded configured plugin {}!", plugin);
	}

	public Map<String, String> getLastDownloadedVersions() {
		final Map<String, String> map = new HashMap<>();
		final List<String> entries = configuration.getStringList("lastDownloadedVersions");

		for (String entry : entries) {
			final String[] split = entry.split(":");

			map.put(split[0], split[1]);
		}

		return map;
	}

	public void saveLastDownloadedVersions(Map<String, String> lastDownloadedVersions) {
		final List<String> strings = new ArrayList<>();

		for (Entry<String, String> entry : lastDownloadedVersions.entrySet()) {
			strings.add(entry.getKey() + ":" + entry.getValue());
		}

		configuration.set("lastDownloadedVersions", strings);
		saveConfig();
	}

	private void downloadNewPlugins() {
		for (ManagedPlugin managedPlugin : managedPlugins) {
			if (!managedPlugin.getFile().exists()) {
				final ArtifactIdentity identity = managedPlugin.getArtifactIdentity();
				logger.info("Downloading new plugin {}...", identity.get());
				try {
					final Release latestRelease = managedPlugin.getRequestMaker()
						.getLatestRelease();
					final Map<String, String> lastDownloadedPluginVersions = getLastDownloadedVersions();

					lastDownloadedPluginVersions.put(identity.getArtifactName(),
						latestRelease.getTagName());

					saveLastDownloadedVersions(lastDownloadedPluginVersions);

					managedPlugin.getRequestMaker().downloadRelease(latestRelease, pluginDirectory);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		}
	}

	private void enablePlugins() {
		for (ManagedPlugin managedPlugin : managedPlugins) {
			final String identity = managedPlugin.getArtifactIdentity().get();

			logger.info("Enabling managed plugin {}...", identity);
			try {
				managedPlugin.load();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			logger.info("Enabled {}!", identity);
		}
	}

}
