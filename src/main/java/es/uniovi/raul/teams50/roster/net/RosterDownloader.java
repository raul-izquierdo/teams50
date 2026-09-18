package es.uniovi.raul.teams50.roster.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

/**
 * This class is responsible for downloading the roster file from a Classroom 50 GitHub repository.
 */
public class RosterDownloader {

    // The repository is always "classroom50"; only the organization and the classroom (subfolder) vary.
    private static final String REPO = "classroom50";
    private static final String ROSTER_FILE = "roster.csv";

    // GitHub Contents API endpoint, with the "raw" media type to get the file content directly instead of base64+JSON.
    private static final String CONTENTS_API_URL = "https://api.github.com/repos/%s/%s/contents/%s/%s";

    public static String downloadRoster(String token, String organization, String classroom)
            throws RosterDownloadException {

        var url = CONTENTS_API_URL.formatted(organization, REPO, classroom, ROSTER_FILE);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.raw+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();

        try (var client = HttpClient.newHttpClient()) {

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RosterDownloadException(
                        "Could not download '%s' from '%s/%s' in organization '%s' (HTTP %d): %s"
                                .formatted(ROSTER_FILE, REPO, classroom, organization, response.statusCode(),
                                        response.body()));

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RosterDownloadException("Error downloading '%s' from '%s/%s' in organization '%s': %s"
                    .formatted(ROSTER_FILE, REPO, classroom, organization, e.getMessage()), e);
        }
    }

    /**
     * Exception thrown when there is an error downloading the roster file from GitHub.
     */
    public static class RosterDownloadException extends Exception {

        public RosterDownloadException(String message) {
            super(message);
        }

        public RosterDownloadException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
