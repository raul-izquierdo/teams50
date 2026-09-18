package es.uniovi.raul.teams50.roster.parser;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;

public class RosterParser {

    public static List<Student> parseRoster(Reader reader, IssuesTracker issueTracker)
            throws IOException {

        List<Student> entries = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

            for (CSVRecord csvRecord : parser) {

                if (skipStudent(csvRecord, parser.getCurrentLineNumber(), issueTracker))
                    continue; // ignore invalid entries

                String username = findValue(csvRecord, "username").get(); // required for students
                String section = findValue(csvRecord, "section").get(); // required for students
                Optional<String> firstName = findValue(csvRecord, "first_name");

                var rosterEntry = new Student(username, section, firstName);

                entries.add(rosterEntry);
            }
        }
        return entries;
    }

    // Skip if the role is not "student", there is no username or there is no section (group).
    private static boolean skipStudent(CSVRecord csvRecord, long currentLineNumber, IssuesTracker issueTracker) {

        // Skip if the role is not "student"
        String role = findValue(csvRecord, "role").orElse("student");
        if (!"student".equals(role)) {
            issueTracker.notifyNonStudent(currentLineNumber, role);
            return true;
        }

        // Skip if the username is empty
        var username = findValue(csvRecord, "username");
        if (username.isEmpty()) {
            issueTracker.notifyMissingUsername(currentLineNumber);
            return true;
        }

        // Skip if the section/group is empty
        var section = findValue(csvRecord, "section");
        if (section.isEmpty()) {
            issueTracker.notifyMissingSection(currentLineNumber);
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the specified column in the CSV record. Used with optional columns. If the value is blank, returns an empty Optional.
     */
    private static Optional<String> findValue(CSVRecord csvRecord, String columnName) {
        try {
            String value = csvRecord.get(columnName);

            if (value == null || value.isBlank())
                return Optional.empty();

            return Optional.of(value);

        } catch (IllegalArgumentException e) { // column not found
            return Optional.empty();
        }
    }

}
