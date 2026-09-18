// CHECKSTYLE:OFF
package es.uniovi.raul.teams50.core;

import java.util.*;

/** Simple {@link Logger} that just records the messages it receives, so tests can assert on them. */
final class TestLogger implements Logger {

    final List<String> infoMessages = new ArrayList<>();
    final List<String> errorMessages = new ArrayList<>();

    @Override
    public void info(String message) {
        infoMessages.add(message);
    }

    @Override
    public void error(String message) {
        errorMessages.add(message);
    }
}
// CHECKSTYLE:ON
