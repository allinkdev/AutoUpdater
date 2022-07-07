package me.allinkdev.autoupdater.common.response;

import lombok.Data;

@Data
public class Asset {

	private final String url;
	private final int id;
	private final String nodeId;
	private final String name;
	private final String label;
	private final Author uploader;
	private final String contentType;
	private final String state;
	private final int size;
	private final int downloadCount;
	private final String createdAt;
	private final String updatedAt;
	private final String browserDownloadUrl;
}
