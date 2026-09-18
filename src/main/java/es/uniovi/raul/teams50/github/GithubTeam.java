package es.uniovi.raul.teams50.github;

/**
 * Store for the information of a team in a GitHub organization.
 *
 * Each team has a display name and a slug (unique identifier used in GitHub API calls).
 *
 * @param organization the organization to which the team belongs
 * @param name the display name of the team
 * @param slug the unique identifier (slug) of the team
 */
public record GithubTeam(String organization, String name, String slug) {

    public GithubTeam {

        if (organization == null || organization.isBlank())
            throw new IllegalArgumentException("Organization cannot be null or blank.");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Display name cannot be null or blank.");

        if (slug == null || slug.isBlank())
            throw new IllegalArgumentException("Slug cannot be null or blank.");
    }
}
