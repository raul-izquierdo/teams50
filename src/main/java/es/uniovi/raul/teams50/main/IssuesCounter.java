package es.uniovi.raul.teams50.main;

import es.uniovi.raul.teams50.roster.parser.IssuesTracker;

/**
 * A class to track issues found in the roster.
 */
public final class IssuesCounter implements IssuesTracker {

    private int missingUsernameCount = 0;
    private int missingSectionCount = 0;
    private int nonStudentCount = 0;

    @Override
    public void notifyMissingUsername(long row) {
        missingUsernameCount++;
    }

    @Override
    public void notifyMissingSection(long row) {
        missingSectionCount++;
    }

    @Override
    public void notifyNonStudent(long row, String role) {
        nonStudentCount++;
    }

    public int getMissingUsernameCount() {
        return missingUsernameCount;
    }

    public int getMissingSectionCount() {
        return missingSectionCount;
    }

    public int getNonStudentCount() {
        return nonStudentCount;
    }
}
