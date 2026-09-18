package es.uniovi.raul.teams50.model;

import java.io.IOException;
import java.util.List;

import es.uniovi.raul.teams50.github.*;
import es.uniovi.raul.teams50.github.GithubApi.GithubApiException;

/**
 * Represents a team in the GitHub organization.
 *
 * It builds upon the information provided by the GithubTeam record, adding methods to simplify the management of team
 * members (fetching, adding and removing students). This operations could be performed directly through the GithubApi,
 * but this class collects all the information that, otherwise, would be passed as separate parameters.
 *
 */
public final class Team {

    private Organization organization;
    private GithubTeam githubTeam;

    public Team(Organization organization, GithubTeam githubTeam) {
        if (githubTeam == null)
            throw new IllegalArgumentException("GithubTeam cannot be null.");
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null.");

        this.githubTeam = githubTeam;
        this.organization = organization;
    }

    public String getName() {
        return githubTeam.name();
    }

    public Organization getOrganization() {
        return organization;
    }

    /**
    * Returns a list of both accepted members and pending invitees of this team.
    *
    * Users with pending invitations are included in the returned list, so it represents all users associated with the
    * team, whether they have accepted the invitation or not.
    *
    * @return List of GitHub usernames (logins) in the team, including pending invitees
    * @throws GithubApiException if the operation is rejected by the GitHub API
    *
    */
    public List<String> fetchTeamMembers()
            throws GithubApiException, IOException, InterruptedException {

        List<String> teamUsernames = getGithubApi().fetchMembers(githubTeam.organization(), githubTeam.slug());

        // getTeamMembers returns only the members that have accepted the invitation. We also need to consider the pending invitations.
        List<String> pendingInvitationMembers = getGithubApi().fetchTeamInvitations(githubTeam.organization(),
                githubTeam.slug());

        teamUsernames.addAll(pendingInvitationMembers);

        return teamUsernames;
    }

    /**
     * Invites (adds) a member to the team.
     *
     * This method will NOT add the member to the organization itself; it only adds them to this specific team. The
     * member must already be a member of the organization to be added to the team..
     *
     * If the student is already a member or has a pending invitation, the operation is treated as successful
     * (idempotent behavior).
     *
     * @param githubUsername the GitHub username of the student to be invited to the team
     * @throws GithubApiException if the operation is rejected by the GitHub API
     *
     */
    public void inviteStudent(String githubUsername)
            throws GithubApiException, IOException, InterruptedException {

        getGithubApi().inviteUsernameToTeam(githubTeam.organization(), githubTeam.slug(), githubUsername);
    }

    /**
     * Removes a student from the team.
     *
     * This method will NOT remove the student from the organization itself; it only removes them from this specific team.
     *
     * If the student is not a member or invitee of the team, the operation is treated as successful (idempotent behavior).
     *
     * @param githubUsername the GitHub username of the student to be removed from the team
     * @throws GithubApiException if the operation is rejected by the GitHub API
     *
     */
    public void removeStudent(String githubUsername)
            throws GithubApiException, IOException, InterruptedException {

        getGithubApi().removeMemberFromTeam(githubTeam.organization(), githubTeam.slug(), githubUsername);
    }

    private GithubApi getGithubApi() {
        return organization.getGithubApi();
    }

}
