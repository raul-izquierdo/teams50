package es.uniovi.raul.teams50.core;

/**
 * A simple logger that outputs messages to the console.
 *
 * Its purpose is to provide a way to log information and error messages during the execution of the application. Use
 * {@link NullLogger} if you want to disable logging.
 */
public interface Logger {
    void info(String message);

    void error(String message);
}
