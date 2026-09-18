package es.uniovi.raul.teams50.roster.parser;

import java.util.*;

public record Student(String username, String section, Optional<String> firstName) {

    public Student {
        Objects.requireNonNull(username);
        Objects.requireNonNull(section);
        Objects.requireNonNull(firstName);
    }

}
