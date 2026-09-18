package es.uniovi.raul.teams50.core;

import static java.util.stream.Stream.*;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.teams50.github.GithubApi.GithubApiException;
import es.uniovi.raul.teams50.model.*;
import es.uniovi.raul.teams50.roster.parser.Student;

/**
 * Contains the core logic of the application, which is responsible for synchronizing the roster with the GitHub organization.
 */
public final class Core {

    public static void run(Organization organization, List<Student> roster)
            throws GithubApiException, IOException, InterruptedException {

        run(organization, roster, new NullLogger());
    }

    /**
     * Entry point for the core logic of the application.
     */
    public static void run(Organization organization, List<Student> roster, Logger logger)
            throws GithubApiException, IOException, InterruptedException {

        var organizationTeams = organization.fetchTeams();

        notifyUnusedTeams(organizationTeams, roster, logger);

        // Crear lista con los section que están en el roster pero no están en la organización
        List<String> sectionsWithoutTeams = findSectionsWithoutTeams(roster, organizationTeams);

        // Crear los equipos que faltan
        List<Team> newTeams = createTeamsForSections(organization, sectionsWithoutTeams, logger);

        // Join organizationTeams and newTeams into a single list.
        var updatedTeams = concat(organizationTeams.stream(), newTeams.stream()).toList();

        updateTeamsMembers(updatedTeams, roster, logger);

        logger.info("\nSynchronization completed successfully.");
    }

    // Imprime en el logger los equipos que están en la organización pero no tienen ningún estudiante en el roster
    private static void notifyUnusedTeams(List<Team> organizationTeams, List<Student> roster, Logger logger) {

        // Teams with no section in the roster
        List<String> unusedTeams = organizationTeams.stream()
                .map(Team::getName)
                .filter(teamName -> roster.stream()
                        .map(Student::section)
                        .noneMatch(section -> section.equalsIgnoreCase(teamName)))
                .toList();

        if (!unusedTeams.isEmpty()) {
            logger.info("\n## The following teams are not used by any student in the roster:");
            unusedTeams.forEach(teamName -> logger.info(" - " + teamName));
        }
    }

    // Returns a list of section names that are present in the roster but do not have corresponding teams in the organization
    private static List<String> findSectionsWithoutTeams(List<Student> roster, List<Team> organizationTeams) {

        return getSectionsInRoster(roster).stream()
                .filter(section -> organizationTeams.stream()
                        .map(Team::getName)
                        .noneMatch(teamName -> teamName.equalsIgnoreCase(section)))
                .toList();
    }

    // Creates new teams for the specified sections
    private static ArrayList<Team> createTeamsForSections(Organization organization, List<String> sections,
            Logger logger)
            throws GithubApiException, IOException, InterruptedException {

        logger.info("\n## Creating new teams for the following sections:");

        var newTeams = new ArrayList<Team>();
        for (String section : sections) {
            Team newTeam = organization.createTeam(section);
            logger.info("  >> Created team: " + newTeam.getName());
            newTeams.add(newTeam);
        }

        if (newTeams.isEmpty())
            logger.info("  >> No new teams were needed.\n");

        return newTeams;
    }

    private static void updateTeamsMembers(List<Team> teams, List<Student> roster, Logger logger)
            throws GithubApiException, IOException, InterruptedException {

        // Buscar los objetos Team correspondientes a los section (String) que están en el roster
        List<Team> teamsInRoster = getSectionsInRoster(roster).stream()
                .map(section -> getTeam(teams, section))
                .toList();

        logger.info("\n## Updating team members based on the roster...");
        for (Team team : teamsInRoster)
            updateTeamMembers(team, roster, logger);

    }

    private static void updateTeamMembers(Team team, List<Student> roster, Logger logger)
            throws GithubApiException, IOException, InterruptedException {

        // Get the list of students in the roster that belong to this team
        List<Student> rosterStudentsInTeam = roster.stream()
                .filter(student -> student.section().equalsIgnoreCase(team.getName()))
                .toList();

        // Get the list of members currently in the team (including pending invitations)
        List<String> teamMembers = team.fetchTeamMembers();

        // Invite students that are in the roster but not in the team
        for (Student student : rosterStudentsInTeam)
            if (!teamMembers.contains(student.username())) {
                team.inviteStudent(student.username());
                logger.info("  >> Invited student '" + student.firstName().orElse("<no name>") + "' to team '"
                        + team.getName() + "'");
            }

        // Remove members that are currently in the team but not in the roster
        for (String member : teamMembers) {

            boolean isStudentInThisSection = rosterStudentsInTeam.stream()
                    .anyMatch(student -> student.username().equals(member));

            if (isStudentInThisSection)
                continue;

            // If the student is not in the whole roster, also remove him from the organization
            boolean removeFromOrg = roster.stream()
                    .noneMatch(student -> student.username().equals(member));

            removeMember(team, member, removeFromOrg, logger);
        }
    }

    // Removes a member from the team and, if indicated, from the organization too.
    // Failures are logged and do not stop the processing of the remaining members.
    // `removeFromOrg` indicates whether the member should also be removed from the organization
    private static void removeMember(Team team, String member, boolean removeFromOrg, Logger logger)
            throws IOException, InterruptedException {

        try {
            team.removeStudent(member);
            logger.info("  >> Removed username '" + member + "' from team '" + team.getName() + "'");
        } catch (GithubApiException e) {
            logger.error("  >> Failed to remove username '" + member + "' from team '" + team.getName()
                    + "': " + e.getMessage());
            return;
        }

        if (!removeFromOrg)
            return;

        try {
            team.getOrganization().removeStudent(member);
            logger.info("        ... and from organization '" + team.getOrganization().getName() + "'");
        } catch (GithubApiException e) {
            logger.error("        ... but failed to remove from organization '"
                    + team.getOrganization().getName() + "': " + e.getMessage());
        }
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods
    //# ------------------------------------------------------------------

    // Get all the sections (groups) appearing in the roster
    private static List<String> getSectionsInRoster(List<Student> roster) {

        return roster.stream()
                .map(Student::section)
                .distinct()
                .toList();
    }

    // Finds the Team object with the given name
    private static Team getTeam(List<Team> teams, String name) {
        return teams.stream()
                .filter(team -> team.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team '" + name + "' does not exist."));
    }
}
