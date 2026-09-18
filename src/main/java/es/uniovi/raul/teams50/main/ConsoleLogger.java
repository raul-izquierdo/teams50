package es.uniovi.raul.teams50.main;

import es.uniovi.raul.teams50.core.Logger;

/**
 * Simple logger that outputs messages to the console.
 */
public final class ConsoleLogger implements Logger {

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(" ---------> [ERROR] " + message);
    }
}
