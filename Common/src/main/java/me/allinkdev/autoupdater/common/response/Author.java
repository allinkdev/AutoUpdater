package me.allinkdev.autoupdater.common.response;

import lombok.Data;

@Data
public class Author {

	private final String login;
	private final int id;
	private final String node_id;
	private final String avatarUrl;
	private final String gravatarId;
	private final String url;
	private final String htmlUrl;
	private final String followersUrl;
	private final String followingUrl;
	private final String gistsUrl;
	private final String starredUrl;
	private final String subscriptionsUrl;
	private final String organizationsUrl;
	private final String reposUrl;
	private final String eventsUrl;
	private final String receivedEventsUrl;
	private final String type;
	private final boolean siteAdmin;
}
