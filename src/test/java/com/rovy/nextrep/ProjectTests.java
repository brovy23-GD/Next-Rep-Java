package com.rovy.nextrep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Dependency-free test runner; exits with nonzero status on any failure. */
public final class ProjectTests {
    static int passed = 0;
    static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
        passed++;
    }
    static void fails(Runnable action, String message) {
        boolean failed = false;
        try { action.run(); } catch (IllegalArgumentException ex) { failed = true; }
        check(failed, message);
    }

    public static void main(String[] args) throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 22);
        WorkoutEntry e = WorkoutEntry.create(date, "  Alex ", "Shooting", 100, 45.5);
        check(e.athlete().equals("Alex"), "trim athlete name");
        fails(() -> WorkoutEntry.create(date, "", "Shooting", 1, 30), "empty athlete rejected");
        fails(() -> WorkoutEntry.create(date, "Alex", "Shooting", -1, 30), "negative reps rejected");
        fails(() -> WorkoutEntry.create(date, "Alex", "Shooting", 1, Double.NaN), "NaN rejected");
        fails(() -> WorkoutEntry.create(date, "Alex", "Shooting", 1, 0), "zero minutes rejected");
        TrainingTracker tracker = new TrainingTracker();
        tracker.add(e);
        tracker.add(WorkoutEntry.create(date.minusDays(1), "alex", "Defense", 50, 25));
        tracker.add(WorkoutEntry.create(date, "Morgan", "Defense", 10, 15));
        check(tracker.all().size() == 3, "add");
        check(tracker.forAthlete("ALEX").size() == 2, "case-insensitive athlete filtering");
        TrainingTracker.Summary s = tracker.summaryFor("alex");
        check(s.sessions() == 2, "session count");
        check(s.repetitions() == 150, "repetitions sum");
        check(Math.abs(s.minutes() - 70.5) < 0.001, "minutes total");
        fails(() -> tracker.add(e), "duplicate IDs rejected");
        check(!tracker.delete(UUID.randomUUID()), "missing delete returns false");
        Path file = Files.createTempFile("nextrep-tests-", ".csv");
        try {
            CsvRepository.save(file, tracker.all());
            List<WorkoutEntry> loaded = CsvRepository.load(file);
            check(loaded.size() == 3, "CSV roundtrip row count");
            check(loaded.contains(e), "CSV roundtrip fidelity");
            Files.writeString(file, "wrong-header\n");
            boolean badHeader = false;
            try { CsvRepository.load(file); } catch (IOException ex) { badHeader = true; }
            check(badHeader, "bad CSV header detected");
        } finally { Files.deleteIfExists(file); }
        check(tracker.delete(e.id()), "delete existing");
        check(tracker.all().size() == 2, "deleted item removed");
        System.out.println("PASS: " + passed + " assertions");
    }
}
