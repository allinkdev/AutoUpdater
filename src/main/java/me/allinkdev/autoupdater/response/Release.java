package me.allinkdev.autoupdater.response;

import java.util.List;
import lombok.Data;

@Data
public class Release {

	private final String url;
	private final String assetsUrl;
	private final String uploadUrl;
	private final String htmlUrl;
	private final int id;
	private final Author author;
	private final String nodeId;
	private final String tagName;
	private final String targetCommitish;
	private final String name;
	private final boolean draft;
	private final boolean preRelease;
	private final String createdAt;
	private final String publishedAt;
	private final List<Asset> assets;
	private final String tarballUrl;
	private final String zipballUrl;
	private final String body;
	private final Object reactions;
}
