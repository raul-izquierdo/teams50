// CHECKSTYLE:OFF
package es.uniovi.raul.teams50.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

import es.uniovi.raul.teams50.github.FakeGithubApi;
import es.uniovi.raul.teams50.model.Organization;
import es.uniovi.raul.teams50.roster.parser.Student;

/**
 * Tests for {@link Core#run}, using {@link FakeGithubApi} to simulate a GitHub organization and verify the exact
 * sequence of API calls performed.
 */
class CoreTest {

    private static final String ORGANIZATION = "acme";

    @Test
    void invitingSameStudentTwice_doesNotSendDuplicateInvitation() throws Exception {

        var api = new FakeGithubApi();
        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        var roster = List.of(new Student("alice", "SectionA", Optional.of("Alice")));

        Core.run(org, roster, logger);

        assertEquals(1, api.createTeamCalls.size());
        assertEquals(List.of("sectiona:alice"), api.inviteCalls);

        // Running again with the same roster: alice is already invited to the team
        Core.run(org, roster, logger);

        assertEquals(1, api.createTeamCalls.size(), "The team should not be created again");
        assertEquals(1, api.inviteCalls.size(), "A student already in the team must not be invited again");
        assertTrue(api.removeFromTeamCalls.isEmpty());
        assertTrue(api.removeFromOrgCalls.isEmpty());
    }

    @Test
    void studentAlreadyAcceptedInTeam_isNotInvitedAgain() throws Exception {

        var api = new FakeGithubApi();
        var team = api.seedTeam(ORGANIZATION, "SectionA");
        api.seedAcceptedMember(team.slug(), "alice");

        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        var roster = List.of(new Student("alice", "SectionA", Optional.of("Alice")));

        Core.run(org, roster, logger);

        assertTrue(api.inviteCalls.isEmpty());
        assertTrue(api.removeFromTeamCalls.isEmpty());
        assertTrue(api.removeFromOrgCalls.isEmpty());
    }

    @Test
    void studentRemovedFromRoster_isRemovedFromTeamAndOrganization() throws Exception {

        var api = new FakeGithubApi();
        var team = api.seedTeam(ORGANIZATION, "SectionA");
        api.seedAcceptedMember(team.slug(), "bob");

        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        // Carol keeps the section alive in the roster; bob no longer appears anywhere
        var roster = List.of(new Student("carol", "SectionA", Optional.of("Carol")));

        Core.run(org, roster, logger);

        assertEquals(List.of(team.slug() + ":bob"), api.removeFromTeamCalls);
        assertEquals(List.of("bob"), api.removeFromOrgCalls);
        assertEquals(List.of(team.slug() + ":carol"), api.inviteCalls);
        assertFalse(api.isOrganizationMember("bob"));
    }

    @Test
    void studentChangesSection_invitedToNewTeam_keptInOrganization() throws Exception {

        var api = new FakeGithubApi();
        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        Core.run(org, List.of(new Student("dave", "SectionA", Optional.of("Dave"))), logger);

        Core.run(org, List.of(new Student("dave", "SectionB", Optional.of("Dave"))), logger);

        assertTrue(api.inviteCalls.contains("sectionb:dave"),
                "Dave should be invited to his new team");
        assertTrue(api.removeFromOrgCalls.isEmpty(),
                "Dave still appears in the roster (under a different section), so he must not be removed "
                        + "from the organization");

        // By design: a section whose team is not referenced by any student in the current roster is left
        // untouched (only reported as unused). Team cleanup, if desired, is a manual step.
        assertTrue(api.removeFromTeamCalls.isEmpty(),
                "SectionA has no students left in the roster, so its team is not touched automatically");
    }

    @Test
    void studentChangesSection_oldSectionStillHasStudents_removedFromOldTeam() throws Exception {

        var api = new FakeGithubApi();
        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        Core.run(org, List.of(
                new Student("dave", "SectionA", Optional.of("Dave")),
                new Student("erin", "SectionA", Optional.of("Erin"))), logger);

        Core.run(org, List.of(
                new Student("dave", "SectionB", Optional.of("Dave")),
                new Student("erin", "SectionA", Optional.of("Erin"))), logger);

        assertTrue(api.removeFromTeamCalls.contains("sectiona:dave"),
                "Dave should be removed from his previous team when he changes section");
        assertTrue(api.inviteCalls.contains("sectionb:dave"),
                "Dave should be invited to his new team");
        assertTrue(api.removeFromOrgCalls.isEmpty(),
                "Dave still appears in the roster (under a different section), so he must not be removed "
                        + "from the organization");
    }

    @Test
    void runningTheSameRosterTwice_producesNoFurtherChanges() throws Exception {

        var api = new FakeGithubApi();
        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        var roster = List.of(
                new Student("alice", "SectionA", Optional.of("Alice")),
                new Student("bob", "SectionB", Optional.of("Bob")));

        Core.run(org, roster, logger);

        int createdAfterFirstRun = api.createTeamCalls.size();
        int invitedAfterFirstRun = api.inviteCalls.size();

        Core.run(org, roster, logger);

        assertEquals(createdAfterFirstRun, api.createTeamCalls.size(), "No new team should be created");
        assertEquals(invitedAfterFirstRun, api.inviteCalls.size(), "No student should be invited again");
        assertTrue(api.removeFromTeamCalls.isEmpty());
        assertTrue(api.removeFromOrgCalls.isEmpty());
    }

    @Test
    void multipleNewSections_createTeamForEachOne() throws Exception {

        var api = new FakeGithubApi();
        var org = new Organization(ORGANIZATION, api);
        var logger = new TestLogger();

        var roster = List.of(
                new Student("alice", "SectionA", Optional.of("Alice")),
                new Student("bob", "SectionB", Optional.of("Bob")));

        Core.run(org, roster, logger);

        assertEquals(2, api.createTeamCalls.size());
        assertTrue(api.createTeamCalls.containsAll(List.of("SectionA", "SectionB")));
        assertTrue(api.inviteCalls.contains("sectiona:alice"));
        assertTrue(api.inviteCalls.contains("sectionb:bob"));
    }
}
// CHECKSTYLE:ON
