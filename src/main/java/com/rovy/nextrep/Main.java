package com.rovy.nextrep;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Zero-dependency CLI; all writes happen through CsvRepository. */
public final class Main {
    private static void usage() {
        System.out.println("NextRep Java - training session tracker\n" +
            "  add <athlete> <exercise> <reps> <minutes> [YYYY-MM-DD]\n" +
            "  list [athlete]\n" +
            "  summary <athlete>\n" +
            "  delete <full-uuid>\n" +
            "  demo (show sample analysis without modifying saved data)\n" +
            "Options: --file <path> (must appear first; default data/sessions.csv)");
    }

    public static void main(String[] args) {
        try { run(args); }
        catch (Exception e) { System.err.println("Error: " + e.getMessage()); System.exit(1); }
    }

    static void run(String[] args) throws Exception {
        Path db = Path.of("data/sessions.csv");
        int start = 0;
        if (args.length >= 2 && args[0].equals("--file")) {
            db = Path.of(args[1]); start = 2;
        }
        if (start >= args.length || args[start].equals("help")) { usage(); return; }
        String action = args[start];
        String[] params = java.util.Arrays.copyOfRange(args, start + 1, args.length);
        TrainingTracker tracker = new TrainingTracker();
        if (!action.equals("demo")) for (WorkoutEntry entry : CsvRepository.load(db)) tracker.add(entry);
        switch (action) {
            case "add" -> {
                if (params.length < 4 || params.length > 5) throw new IllegalArgumentException("add requires athlete, exercise, reps, minutes and optional date");
                LocalDate date = params.length == 5 ? LocalDate.parse(params[4]) : LocalDate.now();
                WorkoutEntry entry = WorkoutEntry.create(date, params[0], params[1], Integer.parseInt(params[2]), Double.parseDouble(params[3]));
                tracker.add(entry);
                CsvRepository.save(db, tracker.all());
                System.out.println("Added session " + entry.id());
            }
            case "list" -> {
                if (params.length > 1) throw new IllegalArgumentException("list accepts at most one athlete");
                List<WorkoutEntry> entries = params.length == 1 ? tracker.forAthlete(params[0]) : tracker.all();
                if (entries.isEmpty()) System.out.println("No sessions found");
                else entries.forEach(e -> System.out.printf("%s | %s | %s | %d reps | %.1f min | %s%n", e.date(), e.athlete(), e.exercise(), e.repetitions(), e.minutes(), e.id()));
            }
            case "summary" -> {
                if (params.length != 1) throw new IllegalArgumentException("summary requires an athlete");
                printSummary(params[0], tracker.summaryFor(params[0]));
            }
            case "delete" -> {
                if (params.length != 1) throw new IllegalArgumentException("delete requires full UUID");
                if (!tracker.delete(UUID.fromString(params[0]))) throw new IllegalArgumentException("session not found");
                CsvRepository.save(db, tracker.all());
                System.out.println("Deleted session");
            }
            case "demo" -> {
                tracker.add(WorkoutEntry.create(LocalDate.of(2026, 11, 18), "Alex", "Shooting", 125, 50));
                tracker.add(WorkoutEntry.create(LocalDate.of(2026, 11, 19), "Alex", "Defense", 65, 35));
                tracker.add(WorkoutEntry.create(LocalDate.of(2026, 11, 20), "Alex", "Shooting", 150, 55));
                tracker.all().forEach(e -> System.out.println(e.date() + " | " + e.athlete() + " | " + e.exercise()));
                printSummary("Alex", tracker.summaryFor("Alex"));
            }
            default -> throw new IllegalArgumentException("unknown command: " + action);
        }
    }
    private static void printSummary(String athlete, TrainingTracker.Summary s) {
        System.out.printf("%s: %d session(s), %d reps, %.1f minutes%n", athlete, s.sessions(), s.repetitions(), s.minutes());
        s.exerciseCounts().forEach((k,v) -> System.out.printf("  %s: %d session(s)%n", k,v));
    }
}
