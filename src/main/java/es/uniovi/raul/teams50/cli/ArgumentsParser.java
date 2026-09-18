package es.uniovi.raul.teams50.cli;

import static java.lang.String.*;

import java.io.PrintStream;
import java.util.Optional;

import io.github.cdimascio.dotenv.Dotenv;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

/** Parses and validates command line arguments. */
public class ArgumentsParser {

    /**
     * Parses command line args.
     * Prints usage, version, or errors as needed.
     *
     * @param args the command line arguments
     * @return an Optional containing the parsed Arguments or empty if parsing failed
     */
    public static Optional<Arguments> parse(String[] args) {
        return parse(args, System.out, System.err);
    }

    public static Optional<Arguments> parse(String[] args, PrintStream out, PrintStream err) {

        final Arguments arguments = new Arguments();

        final CommandLine picocli = new CommandLine(arguments)
                .setCaseInsensitiveEnumValuesAllowed(true)
                // .setColorScheme(CommandLine.Help.defaultColorScheme(Help.Ansi.ON))
                .setSeparator(" "); // Use space (`-g file`) instead of "=" (`-g=file`);

        try {
            picocli.parseArgs(args);

            if (picocli.isUsageHelpRequested()) {
                picocli.usage(out);
                return Optional.empty();
            }

            if (picocli.isVersionHelpRequested()) {
                picocli.printVersionHelp(out);
                return Optional.empty();
            }

            ensureRequiredEnvironment(arguments, picocli);

            return Optional.of(arguments);

        } catch (ParameterException ex) {
            System.err.printf("%n[Error] %s%n", ex.getMessage());
            picocli.usage(err);
            return Optional.empty();
        }
    }

    //#  -----------------------------------

    private static void ensureRequiredEnvironment(Arguments arguments, final CommandLine picocli) {

        // If the user provided a local roster file, we don't need to check for GitHub-related arguments.
        if (arguments.rosterFile != null)
            return;

        arguments.token = ensureArgument(arguments.token, "GITHUB_TOKEN", picocli);
        arguments.classroomOrg = ensureArgument(arguments.classroomOrg, "CLASSROOM_ORG", picocli);
        arguments.solutionsOrg = ensureArgument(arguments.solutionsOrg, "SOLUTIONS_ORG", picocli);
        arguments.classroom = ensureArgument(arguments.classroom, "CLASSROOM_NAME", picocli);
    }

    // Helper methods for environment variables
    private static String ensureArgument(String argValue, String envKey, final CommandLine picocli) {
        if (argValue != null)
            return argValue;

        return getEnvironmentVariable(envKey)
                .orElseThrow(() -> new ParameterException(picocli,
                        format("Missing required arguments: %s should be provided either via command line or in a '.env' file",
                                envKey)));
    }

    private static Optional<String> getEnvironmentVariable(String key) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String value = dotenv.get(key);
        if (value == null)
            value = System.getenv(key);
        return Optional.ofNullable(value);
    }

}
