package es.uniovi.raul.teams50.main;

import static es.uniovi.raul.teams50.roster.net.RosterDownloader.*;

import java.io.*;
import java.util.*;

import es.uniovi.raul.teams50.cli.*;
import es.uniovi.raul.teams50.core.Core;
import es.uniovi.raul.teams50.github.*;
import es.uniovi.raul.teams50.github.GithubApi.GithubApiException;
import es.uniovi.raul.teams50.model.Organization;
import es.uniovi.raul.teams50.roster.net.RosterDownloader.RosterDownloadException;
import es.uniovi.raul.teams50.roster.parser.*;

/**
 * Entry point for the app.
 */
public class Main {
    private static final int OK = 0;
    private static final int ERROR = 1;

    public static void main(String[] args) {

        Optional<Arguments> argumentsOpt = ArgumentsParser.parse(args);

        if (argumentsOpt.isEmpty())
            System.exit(ERROR);

        int exitCode;
        try {
            exitCode = loadAndRun(argumentsOpt.get());

            System.out.println("""

                    ---------------------------------------------------------------------------------------------
                    REMEMBER. Students have been invited to join their groups, but they are NOT MEMBERS YET!!!
                    Each student must accept the invitation sent to their email before they appear in the groups.
                    """);

        } catch (Exception e) {
            System.err.printf("%n[Error] %s%n", e.getMessage());
            exitCode = ERROR;
        }

        System.exit(exitCode);
    }

    private static int loadAndRun(Arguments arguments)
            throws IOException, RosterDownloadException, GithubApiException, InterruptedException {

        // Load...
        GithubApi githubApi = createGithubApi(arguments);
        var organization = new Organization(arguments.solutionsOrg, githubApi);

        Reader rosterReader = createRosterReader(arguments);
        List<Student> roster = loadRoster(rosterReader);

        // ... and Run
        var logger = new ConsoleLogger();
        Core.run(organization, roster, logger);

        return OK;
    }

    private static GithubApi createGithubApi(Arguments arguments) {

        GithubApi connection = new GithubApiImpl(arguments.token);
        if (arguments.dryRun) {
            System.out.println("\n[DRY-RUN] No changes will be performed.");
            connection = new GithubApiDryRunDecorator(connection);
        }
        return connection;
    }

    // Determine the source of the roster: either a local file or a GitHub repository.
    private static Reader createRosterReader(Arguments arguments) throws IOException, RosterDownloadException {

        if (arguments.rosterFile != null) {
            System.out.printf("%n## Using local roster file: %s%n", arguments.rosterFile);
            return new FileReader(arguments.rosterFile);
        }

        System.out.printf(
                "%n## No local roster file provided. Proceeding to load roster from GitHub repository '%s' in organization '%s'...%n",
                arguments.classroom, arguments.classroomOrg);

        String rosterCsv = downloadRoster(arguments.token, arguments.classroomOrg, arguments.classroom);
        return new StringReader(rosterCsv);
    }

    private static List<Student> loadRoster(Reader reader) throws IOException {

        var issuesTracker = new IssuesCounter();
        List<Student> roster = RosterParser.parseRoster(reader, issuesTracker);

        // Report issues found during parsing
        System.out.printf("    %d processable roster entries found.%n", roster.size());

        int studentsWithoutFirstName = (int) roster.stream().filter(entry -> entry.firstName().isEmpty()).count();
        if (studentsWithoutFirstName > 0)
            System.out.printf("    of them, %d entries have been accepted without a first name.%n",
                    studentsWithoutFirstName);

        System.out.printf("    %d entries skipped due to missing GitHub username.%n",
                issuesTracker.getMissingUsernameCount());
        System.out.printf("    %d entries skipped due to not being students.%n", issuesTracker.getNonStudentCount());

        return roster;
    }
}
