package es.uniovi.raul.teams50.model;

import java.io.IOException;
import java.util.List;

import es.uniovi.raul.teams50.github.*;
import es.uniovi.raul.teams50.github.GithubApi.GithubApiException;

public final class Organization {

    private String organizationName;
    private GithubApi githubApi;

    public Organization(String name, GithubApi githubApi) throws GithubApiException, IOException, InterruptedException {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Organization name cannot be null or blank.");
        if (githubApi == null)
            throw new IllegalArgumentException("GithubApi cannot be null.");

        this.organizationName = name;
        this.githubApi = githubApi;
    }

    public String getName() {
        return organizationName;
    }

    /**
    * Fetches all teams in the organization from the GitHub API and updates the internal list of teams.
    *
    * @return A list of Team objects representing the teams in the organization
    * @throws GithubApiException if the operation is rejected by the GitHub API
    * @throws IOException if an I/O error occurs
    * @throws InterruptedException if the thread is interrupted
    */
    public List<Team> fetchTeams()
            throws GithubApiException, IOException, InterruptedException {

        return githubApi.fetchTeams(organizationName).stream()
                .map(githubTeam -> new Team(this, githubTeam))
                .toList();

    }

    public Team createTeam(String teamName)
            throws GithubApiException, IOException, InterruptedException {

        GithubTeam newGithubTeam = githubApi.createTeam(organizationName, teamName);

        return new Team(this, newGithubTeam);
    }

    /**
     * Removes a user from the given organization and, therefore, from all teams in that organization.
     *
     * @param githubUsername the GitHub username of the user to be removed from the organization
     * @throws GithubApiException if the operation is rejected by the GitHub API
     *
     */
    public void removeStudent(String githubUsername)
            throws GithubApiException, IOException, InterruptedException {

        githubApi.removeMemberFromOrganization(organizationName, githubUsername);
    }

    //# ------------------------------------------------------------------
    //# Internal methods
    //# ------------------------------------------------------------------

    GithubApi getGithubApi() { // Acceso package-private para que Team pueda acceder a él
        return githubApi;
    }

}
