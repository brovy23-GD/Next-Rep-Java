package com.rovy.nextrep;

import java.util.*;
import java.util.stream.Collectors;

/** Business logic: deduplication, filtering, and activity summaries. */
public final class TrainingTracker {
    private final Map<UUID, WorkoutEntry> sessions = new LinkedHashMap<>();

    public void add(WorkoutEntry entry) {
        Objects.requireNonNull(entry, "entry");
        if (sessions.putIfAbsent(entry.id(), entry) != null) {
            throw new IllegalArgumentException("duplicate session ID");
        }
    }

    public boolean delete(UUID id) { return sessions.remove(id) != null; }
    public List<WorkoutEntry> all() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(WorkoutEntry::date).reversed().thenComparing(WorkoutEntry::athlete))
                .toList();
    }
    public List<WorkoutEntry> forAthlete(String name) {
        return all().stream().filter(e -> e.athlete().equalsIgnoreCase(name.trim())).toList();
    }
    public Summary summaryFor(String name) {
        List<WorkoutEntry> list = forAthlete(name);
        int reps = list.stream().mapToInt(WorkoutEntry::repetitions).sum();
        double minutes = list.stream().mapToDouble(WorkoutEntry::minutes).sum();
        Map<String, Long> byExercise = list.stream().collect(Collectors.groupingBy(
                WorkoutEntry::exercise, TreeMap::new, Collectors.counting()));
        return new Summary(list.size(), reps, minutes, Collections.unmodifiableMap(byExercise));
    }
    public record Summary(int sessions, int repetitions, double minutes, Map<String, Long> exerciseCounts) { }
}
