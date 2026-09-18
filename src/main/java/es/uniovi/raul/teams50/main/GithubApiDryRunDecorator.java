package es.uniovi.raul.teams50.main;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.teams50.github.*;

/**
 * Decorator for GithubApi that disables write operations while allowing reads.
 * Useful for dry-run mode to see the actions that would be performed without
 * making any changes in GitHub.
 */
public final class GithubApiDryRunDecorator implements GithubApi {

    private final GithubApi delegate;

    public GithubApiDryRunDecorator(GithubApi delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("Delegate cannot be null.");
        this.delegate = delegate;
    }

    // Read operations: delegate
    @Override
    public List<GithubTeam> fetchTeams(String organization)
            throws GithubApiException, IOException, InterruptedException {
        return delegate.fetchTeams(organization);
    }

    @Override
    public List<String> fetchMembers(String organization, String teamSlug)
            throws GithubApiException, IOException, InterruptedException {
        try {
            return delegate.fetchMembers(organization, teamSlug);
        } catch (GithubApiException e) {
            // Esto es para permitir que el dry-run funcione aunque el equipo no exista en GitHub. En ese caso, se devuelve una lista vacía.
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> fetchTeamInvitations(String organization, String teamSlug)
            throws GithubApiException, IOException, InterruptedException {
        try {
            return delegate.fetchTeamInvitations(organization, teamSlug);
        } catch (GithubApiException e) {
            // Esto es para permitir que el dry-run funcione aunque el equipo no exista en GitHub. En ese caso, se devuelve una lista vacía.
            return Collections.emptyList();
        }
    }

    // Write operations: no-ops
    @Override
    public GithubTeam createTeam(String organization, String teamDisplayName)
            throws GithubApiException, IOException, InterruptedException {
        // Dry-run: do not create anything. Return the team name as the slug.
        return new GithubTeam(organization, teamDisplayName, teamDisplayName);
    }

    @Override
    public void inviteUsernameToTeam(String organization, String teamSlug, String githubUsername)
            throws GithubApiException, IOException, InterruptedException {
        // Dry-run: do nothing
    }

    @Override
    public void removeMemberFromTeam(String organization, String teamSlug, String githubUsername)
            throws GithubApiException, IOException, InterruptedException {
        // Dry-run: do nothing
    }

    @Override
    public void removeMemberFromOrganization(String organization, String githubUsername)
            throws GithubApiException, IOException, InterruptedException {
        // Dry-run: do nothing
    }
}
