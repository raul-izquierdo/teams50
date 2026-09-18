package es.uniovi.raul.teams50.github;

import java.io.IOException;
import java.util.List;

/**
 * Interface for interacting with the GitHub API to manage teams and their members within an organization.
 */
public interface GithubApi {

    /**
     * Downloads the list of teams from the specified organization.
     *
     * @param organization Organization name
     * @return List of teams in the organization
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    List<GithubTeam> fetchTeams(String organization)
            throws GithubApiException, IOException, InterruptedException;

    /**
     * Creates a new team in the specified organization with the given display name.
     *
     * If the team already exists, returns a RejectedOperationException. If it were useful
     * to distinguish between this case and other errors, it should be changed in the future.
     * Nowdays is not necesary, as trying to create a team that already exists should be an error
     * with the actual implementation of the app.
     *
     * @param organization   Organization name
     * @param teamDisplayName Display name for the new team
     * @return The created GithubTeam object
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    GithubTeam createTeam(String organization, String teamDisplayName)
            throws GithubApiException, IOException, InterruptedException;

    // Finally not needed
    /*
     * Removes a team from the specified organization.
     * If the team already exists, it returns an empty Optional.
     *
     * Notes:
     * - Deleting a team does not remove its members from the organization account itself.
     * - Any pending invitations to that team are effectively invalidated when the team no longer exists.
     *
     * @param organization   Organization name
     * @param teamSlug       Slug of the team to remove
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    // void deleteTeam(String organization, String teamSlug)
    //         throws GithubApiException, IOException, InterruptedException;

    /**
    * Removes a user from the given organization and, therefore, from all teams in that organization.
    * <p>
    * Behavior:
    * - Idempotent: if the user is not an accepted member (e.g., only invited), the call is treated as success but
    *   it does not cancel any existing organization-level invitation.
    * - This method does not manage team invitations. Pending team invitations become irrelevant if the
    *   corresponding team is deleted.
    * - This method does not cancel organization-level invitations. If you need to revoke standalone org invites,
    *   that must be handled via the org invitations API (not exposed here).
    *
    * @param organization Organization name
    * @param githubUsername GitHub username of the user to remove
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws IOException if a network error occurs
    * @throws InterruptedException if the operation is interrupted
    */
    void removeMemberFromOrganization(String organization, String githubUsername)
            throws GithubApiException, IOException, InterruptedException;

    /**
     * Invites (adds) a student to a team in the specified organization.
     * <p>
     * Behavior is idempotent: if the student is already a member, or already has a pending team invitation,
     * the API returns success and no additional action is required.
     * <p>
     * If the operation is rejected by the GitHub API, a {@link RejectedOperationException} is thrown.
     *
     * @param organization   Organization name
     * @param teamSlug       Slug of the team to which the student will be invited
     * @param githubUsername GitHub username of the student
     * @throws IOException if a network error occurs
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws InterruptedException if the operation is interrupted
     */
    void inviteUsernameToTeam(String organization, String teamSlug, String githubUsername)
            throws GithubApiException, IOException, InterruptedException;

    /**
    * Removes a member from a team in the specified organization.
    * <p>
    * If the operation is successful (member is removed or was not a member/invitee), the method returns normally.
    * If the operation is rejected by the GitHub API, a {@link RejectedOperationException} is thrown.
    * <p>
    * Notes:
    * - Removing a student from a team does not remove them from the organization.
    * - If the user had a pending team invitation, the removal is treated as success and the invitation is no longer applicable.
    *
    * @param organization   Organization name
    * @param teamSlug       Slug of the team from which the student will be removed
    * @param githubUsername GitHub username of the student
    * @throws IOException if a network error occurs
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws InterruptedException if the operation is interrupted
    */
    void removeMemberFromTeam(String organization, String teamSlug, String githubUsername)
            throws GithubApiException, IOException, InterruptedException;

    /**
    * Returns a list of GitHub usernames (logins) for the accepted members of a given team in the specified organization.
    * Pending team invitations are not included here.
    *
    * @param organization Organization name
    * @param teamSlug     Slug of the team
    * @return List of GitHub usernames (logins) in the team
    * @throws IOException if a network error occurs
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws UnexpectedFormatException if the response format is unexpected
    * @throws InterruptedException if the operation is interrupted
    */
    List<String> fetchMembers(String organization, String teamSlug)
            throws GithubApiException, IOException, InterruptedException;

    /**
    * Returns a list of GitHub usernames (logins) that have a pending invitation to the given team.
    * These are team-level invitations, not organization-level invitations.
    *
    * @param organization Organization name
    * @param teamSlug     Slug of the team
    * @return List of GitHub usernames (logins) with a pending invitation to the team
    * @throws IOException if a network error occurs
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws UnexpectedFormatException if the response format is unexpected
    * @throws InterruptedException if the operation is interrupted
    */
    List<String> fetchTeamInvitations(String organization, String teamSlug)
            throws GithubApiException, IOException, InterruptedException;

    //# ------------------------------------------------------------------
    //# Exceptions
    //# ------------------------------------------------------------------

    /**
     * Base exception for all GitHub API-related errors.
     */
    class GithubApiException extends Exception {
        public GithubApiException(String message) {
            super(message);
        }

        public GithubApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
    * Exception thrown when the response format is unexpected. Probably the format has changed and this code needs to be updated.
    */
    class UnexpectedFormatException extends GithubApiException {
        public UnexpectedFormatException(String message) {
            super(message);
        }

        public UnexpectedFormatException(String format, Object... args) {
            super(String.format(format, args));
        }
    }

    /**
    * Exception thrown when the operation is rejected by GitHub API.
    */
    class RejectedOperationException extends GithubApiException {
        public RejectedOperationException(String message) {
            super(message);
        }

        public RejectedOperationException(String format, Object... args) {
            super(String.format(format, args));
        }
    }

}
