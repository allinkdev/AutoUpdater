package me.allinkdev.autoupdater.common.request;

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
import lombok.NonNull;
import me.allinkdev.autoupdater.common.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.common.response.Asset;
import me.allinkdev.autoupdater.common.response.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestMaker {

	private static final Path TEMPORARY_DIRECTORY = Path.of(System.getProperty("java.io.tmpdir"));
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final Gson GSON = new GsonBuilder()
		.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		.create();
	private final Logger LOGGER;
	private final ArtifactIdentity identity;

	public RequestMaker(@NonNull ArtifactIdentity identity) {
		this.identity = identity;

		LOGGER = LoggerFactory.getLogger("Request Maker/" + this.identity.get());
	}

	public Release getLatestRelease() throws IOException, InterruptedException {
		LOGGER.info("Discovering latest release of {}", identity.get());

		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(URI.create("https://api.github.com/repos/" + identity.getGroupName() + "/"
				+ identity.getArtifactName()
				+ "/releases/latest"))
			.build();

		final HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
		final String json = response.body();
		final Release release = GSON.fromJson(json, Release.class);

		LOGGER.info("Found latest release of {} to be {}!", identity.get(), release.getName());

		return release;
	}

	public Path downloadAsset(@NonNull String url, @NonNull Path directory)
		throws IOException, InterruptedException {
		return downloadAsset(URI.create(url), directory);
	}

	public Path downloadAsset(@NonNull String url, @NonNull Path directory,
		@NonNull Path temporaryDirectory) throws IOException, InterruptedException {
		return downloadAsset(URI.create(url), directory, temporaryDirectory);
	}

	public Path downloadAsset(@NonNull URI uri, @NonNull Path directory)
		throws IOException, InterruptedException {
		return downloadAsset(uri, directory, TEMPORARY_DIRECTORY);
	}

	private URI getRealUri(@NonNull URI redirectingUri) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(redirectingUri)
			.build();

		final HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

		return URI.create(response.headers().firstValue("location").orElseThrow());
	}

	public Path downloadAsset(@NonNull URI uri, @NonNull Path directory,
		@NonNull Path temporaryDirectory)
		throws IOException, InterruptedException {
		final String outputName = identity.getArtifactName() + ".jar";
		final Path outputPath = directory.resolve(outputName);
		LOGGER.info("Downloading {} (of {}) to {}...", uri.toString(), identity.get(), outputName);

		final HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(getRealUri(uri))
			.build();

		final Path tempName = temporaryDirectory.resolve(
			Paths.get(uri.getPath()).getFileName().toString());
		final HttpResponse<Path> file = HTTP_CLIENT.send(request,
			BodyHandlers.ofFileDownload(temporaryDirectory,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE));

		Files.move(tempName, outputPath);

		LOGGER.info("Downloaded {}!", outputName);
		return file.body();
	}

	public Path downloadRelease(@NonNull Release release, @NonNull Path directory)
		throws IOException, InterruptedException {
		return downloadRelease(release, directory, TEMPORARY_DIRECTORY);
	}

	public Path downloadRelease(@NonNull Release release, @NonNull Path directory,
		@NonNull Path temporaryDirectory) throws IOException, InterruptedException {
		final List<Asset> assets = release.getAssets();

		Asset selectedAsset = null;

		for (Asset asset : assets) {
			if (asset.getContentType().equals("application/java-archive") || (asset.getContentType()
				.equals("application/octet-stream") && asset.getName().endsWith(".jar"))) {
				selectedAsset = asset;
				break;
			}
		}

		assert selectedAsset != null : "Release " + release.getName() + " for " + identity.get();

		return downloadAsset(selectedAsset.getBrowserDownloadUrl(), directory, temporaryDirectory);
	}

	public Path downloadLatestRelease(@NonNull Path directory, @NonNull Path temporaryDirectory)
		throws IOException, InterruptedException, NullPointerException {
		LOGGER.info("Downloading latest release of {}...", identity.get());

		final Release latestRelease = getLatestRelease();

		final Path path = downloadRelease(latestRelease, directory, temporaryDirectory);

		LOGGER.info("Downloaded latest release of {}!", identity.get());

		return path;
	}
}
