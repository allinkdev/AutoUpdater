package me.allinkdev.autoupdater.common.artifact;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArtifactIdentity {

	private final String groupName;
	private final String artifactName;

	public static ArtifactIdentity fromString(String artifactString) {
		final String[] split = artifactString.split(":");

		return new ArtifactIdentity(split[0], split[1]);
	}

	public final String get() {
		return groupName + ":" + artifactName;
	}
}
