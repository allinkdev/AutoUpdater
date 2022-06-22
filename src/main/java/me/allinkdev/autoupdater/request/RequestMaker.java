package me.allinkdev.autoupdater.request;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import lombok.AllArgsConstructor;
import me.allinkdev.autoupdater.AutoUpdater;
import me.allinkdev.autoupdater.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.response.Asset;
import me.allinkdev.autoupdater.response.Release;
import org.slf4j.Logger;

@AllArgsConstructor
public class RequestMaker {

	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final Gson GSON = new GsonBuilder()
		.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		.create();
	private static final AutoUpdater UPDATER = AutoUpdater.getInstance();
	private static final Logger LOGGER = UPDATER.getSLF4JLogger();
	private static final Path PLUGIN_DIRECTORY = UPDATER.getPluginDirectory();
	private final ArtifactIdentity identity;


	public Release getLatestRelease() throws IOException, InterruptedException {
		LOGGER.info("Discovering latest release of {}", identity.get());

		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(URI.create("https://api.github.com/repos/" + identity.getGroupName() + "/" + identity.getArtifactName()
				+ "/releases/latest"))
			.build();

		final HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
		final String json = response.body();
		final Release release = GSON.fromJson(json, Release.class);

		LOGGER.info("Found latest release of {} to be {}!", identity.get(), release.getName());

		return release;
	}

	public Path downloadAsset(Asset asset) throws IOException, InterruptedException {
		return downloadAsset(asset.getBrowserDownloadUrl());
	}

	public Path downloadAsset(String url) throws IOException, InterruptedException {
		return downloadAsset(URI.create(url));
	}

	private URI getRealUri(URI redirectingUri) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(redirectingUri)
			.build();

		final HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

		return URI.create(response.headers().firstValue("location").orElseThrow());
	}

	public Path downloadAsset(URI uri) throws IOException, InterruptedException {
		final String outputName = identity.getArtifactName() + ".jar";
		final Path outputPath = PLUGIN_DIRECTORY.resolve(outputName);
		LOGGER.info("Downloading {} (of {}) to {}...", uri.toString(), identity.get(), outputName);

		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(getRealUri(uri))
			.build();

		final Path tempDirectory = AutoUpdater.getInstance().getDataFolder().toPath().resolve("tmp");
		final Path tempName = tempDirectory.resolve(Paths.get(uri.getPath()).getFileName().toString());
		final HttpResponse<Path> file = HTTP_CLIENT.send(request, BodyHandlers.ofFileDownload(tempDirectory,
			StandardOpenOption.WRITE, StandardOpenOption.CREATE));

		Files.move(tempName, outputPath);

		LOGGER.info("Downloaded {}!", outputName);
		return file.body();
	}

	public Path downloadRelease(Release release) throws IOException, InterruptedException {
		final List<Asset> assets = release.getAssets();

		Asset selectedAsset = null;

		for (Asset asset : assets) {
			if (asset.getContentType().equals("application/java-archive")) {
				selectedAsset = asset;
				break;
			}
		}

		if (selectedAsset == null) {
			throw new NullPointerException(
				"Release %s for %s is null.".formatted(release.getTagName(), identity.get()));
		}

		return downloadAsset(selectedAsset);
	}

	public Path downloadLatestRelease() throws IOException, InterruptedException, NullPointerException {
		LOGGER.info("Downloading latest release of {}...", identity.get());

		final Release latestRelease = getLatestRelease();

		final Path path = downloadRelease(latestRelease);

		LOGGER.info("Downloaded latest release of {}!", identity.get());

		return path;
	}
}
