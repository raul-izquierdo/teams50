// CHECKSTYLE:OFF
package es.uniovi.raul.teams50.github;

import java.util.*;

/**
 * In-memory fake implementation of {@link GithubApi} used to test {@code Core} without performing real network calls.
 *
 * It keeps track of the simulated state of a GitHub organization (teams, accepted members and pending invitations)
 * and records every call made to the API so tests can assert on the exact invocations performed by the code under test.
 */
public final class FakeGithubApi implements GithubApi {

    private final Map<String, GithubTeam> teamsBySlug = new LinkedHashMap<>();
    private final Map<String, Set<String>> acceptedMembersBySlug = new LinkedHashMap<>();
    private final Map<String, Set<String>> pendingInvitationsBySlug = new LinkedHashMap<>();
    private final Set<String> organizationMembers = new LinkedHashSet<>();

    // Recorded invocations, exposed directly for test assertions
    public final List<String> createTeamCalls = new ArrayList<>();
    public final List<String> inviteCalls = new ArrayList<>();
    public final List<String> removeFromTeamCalls = new ArrayList<>();
    public final List<String> removeFromOrgCalls = new ArrayList<>();

    //# ------------------------------------------------------------------
    //# Test setup helpers (not part of the real GitHub API)
    //# ------------------------------------------------------------------

    // Pre-populates the fake organization with an already existing team
    public GithubTeam seedTeam(String organization, String teamName) {

        var team = new GithubTeam(organization, teamName, slugify(teamName));
        teamsBySlug.put(team.slug(), team);
        acceptedMembersBySlug.put(team.slug(), new LinkedHashSet<>());
        pendingInvitationsBySlug.put(team.slug(), new LinkedHashSet<>());

        return team;
    }

    // Pre-populates a team with a member that has already accepted the invitation
    public void seedAcceptedMember(String teamSlug, String githubUsername) {
        acceptedMembersBySlug.get(teamSlug).add(githubUsername);
        organizationMembers.add(githubUsername);
    }

    // Pre-populates a team with a member that has a pending (not yet accepted) invitation
    public void seedPendingInvitation(String teamSlug, String githubUsername) {
        pendingInvitationsBySlug.get(teamSlug).add(githubUsername);
        organizationMembers.add(githubUsername);
    }

    public boolean isOrganizationMember(String githubUsername) {
        return organizationMembers.contains(githubUsername);
    }

    private static String slugify(String teamName) {
        return teamName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    //# ------------------------------------------------------------------
    //# GithubApi implementation
    //# ------------------------------------------------------------------

    @Override
    public List<GithubTeam> fetchTeams(String organization) {

        return teamsBySlug.values().stream()
                .filter(team -> team.organization().equals(organization))
                .toList();
    }

    @Override
    public GithubTeam createTeam(String organization, String teamDisplayName) {

        createTeamCalls.add(teamDisplayName);
        return seedTeam(organization, teamDisplayName);
    }

    @Override
    public void removeMemberFromOrganization(String organization, String githubUsername) {

        removeFromOrgCalls.add(githubUsername);
        organizationMembers.remove(githubUsername);
        acceptedMembersBySlug.values().forEach(members -> members.remove(githubUsername));
        pendingInvitationsBySlug.values().forEach(invitees -> invitees.remove(githubUsername));
    }

    @Override
    public void inviteUsernameToTeam(String organization, String teamSlug, String githubUsername) {

        inviteCalls.add(teamSlug + ":" + githubUsername);
        organizationMembers.add(githubUsername);
        pendingInvitationsBySlug.computeIfAbsent(teamSlug, slug -> new LinkedHashSet<>()).add(githubUsername);
    }

    @Override
    public void removeMemberFromTeam(String organization, String teamSlug, String githubUsername) {

        removeFromTeamCalls.add(teamSlug + ":" + githubUsername);
        acceptedMembersBySlug.getOrDefault(teamSlug, Set.of()).remove(githubUsername);
        pendingInvitationsBySlug.getOrDefault(teamSlug, Set.of()).remove(githubUsername);
    }

    @Override
    public List<String> fetchMembers(String organization, String teamSlug) {
        return new ArrayList<>(acceptedMembersBySlug.getOrDefault(teamSlug, Set.of()));
    }

    @Override
    public List<String> fetchTeamInvitations(String organization, String teamSlug) {
        return new ArrayList<>(pendingInvitationsBySlug.getOrDefault(teamSlug, Set.of()));
    }
}
// CHECKSTYLE:ON
