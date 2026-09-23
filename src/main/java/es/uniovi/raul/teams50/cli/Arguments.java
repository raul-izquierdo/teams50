package es.uniovi.raul.teams50.cli;

import picocli.CommandLine.*;

// CHECKSTYLE:OFF

@Command(name = "teams50", showDefaultValues = true, mixinStandardHelpOptions = true, usageHelpAutoWidth = true, description = Messages.DESCRIPTION, customSynopsis = Messages.USAGE, footer = Messages.CREDITS, versionProvider = PomVersionReader.class)
public class Arguments {

    @Option(names = "-r", description = "Use this local roster file instead of downloading the roster from the Classroom 50 repository.")
    public String rosterFile;

    @Option(names = "-t", description = "GitHub API access token. If not provided, it will try to read from the GITHUB_TOKEN environment variable or from a '.env' file.")
    public String token;

    @Option(names = "-o", description = "GitHub organization for the Classroom 50 repository. If not provided, it will try to read from the CLASSROOM_ORG environment variable or from a '.env' file.")
    public String classroomOrg;

    @Option(names = "-c", description = "Classroom name. If not provided, it will try to read from the CLASSROOM_NAME environment variable or from a '.env' file.")
    public String classroom;

    @Option(names = "-s", description = "GitHub organization where the solutions are stored. If not provided, it will try to read from the SOLUTIONS_ORG environment variable or from a '.env' file.")
    public String solutionsOrg;

    @Option(names = "--dry-run", description = "Do not perform any changes; only read and print the actions that would be performed.")
    public boolean dryRun;

}

class Messages {
    static final String DESCRIPTION = """

            This tool helps you identify which students need to be added, updated, or removed from a Classroom 50 roster.

            For more information, visit: https://github.com/raul-izquierdo/teams50
            """;

    static final String USAGE = "\n\tjava -jar teams50.jar [OPTIONS]\n";

    static final String CREDITS = """

            Escuela de Ingenieria Informatica. Universidad de Oviedo.
            Raúl Izquierdo Castanedo (raul@uniovi.es)
            """;

}

class PomVersionReader implements IVersionProvider {
    public String[] getVersion() throws Exception {
        return new String[] { Arguments.class.getPackage().getImplementationVersion() };
    }
}
