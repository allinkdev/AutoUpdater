package me.allinkdev.autoupdater;

import java.io.File;
import lombok.Data;
import me.allinkdev.autoupdater.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.request.RequestMaker;
import me.allinkdev.autoupdater.utility.PluginUtility;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

@Data
public class ManagedPlugin {

	private final ArtifactIdentity artifactIdentity;
	private final RequestMaker requestMaker;
	private final File file;
	private Plugin plugin;

	public ManagedPlugin(String groupName, String artifactName, File file) {
		this.artifactIdentity = ArtifactIdentity.builder()
			.groupName(groupName)
			.artifactName(artifactName)
			.build();
		this.requestMaker = new RequestMaker(this.artifactIdentity);
		this.file = file;
	}

	public ManagedPlugin(ArtifactIdentity artifactIdentity, File file) {
		this.artifactIdentity = artifactIdentity;
		this.requestMaker = new RequestMaker(this.artifactIdentity);
		this.file = file;
	}

	public void load() throws InvalidPluginException, InvalidDescriptionException {
		plugin = PluginUtility.load(this.file);
	}

	public void unload() throws NoSuchMethodException {
		PluginUtility.unload(this.plugin);
		this.plugin = null;
	}

	public void reload()
		throws InvalidPluginException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvalidDescriptionException {
		this.plugin = PluginUtility.reloadPlugin(this.plugin);
	}
}
