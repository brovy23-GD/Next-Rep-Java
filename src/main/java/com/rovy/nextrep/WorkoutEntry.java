package com.rovy.nextrep;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Immutable validated training session. */
public record WorkoutEntry(UUID id, LocalDate date, String athlete, String exercise, int repetitions, double minutes) {
    public WorkoutEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(date, "date");
        athlete = requireText(athlete, "athlete");
        exercise = requireText(exercise, "exercise");
        if (repetitions < 0) throw new IllegalArgumentException("repetitions cannot be negative");
        if (!Double.isFinite(minutes) || minutes <= 0) throw new IllegalArgumentException("minutes must be a positive finite number");
    }

    private static String requireText(String input, String name) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException(name + " is required");
        String clean = input.trim();
        if (clean.length() > 100 || clean.contains("\n") || clean.contains("\r") || clean.contains("\t"))
            throw new IllegalArgumentException(name + " must be 1-100 characters with no control characters");
        return clean;
    }

    public static WorkoutEntry create(LocalDate date, String athlete, String exercise, int repetitions, double minutes) {
        return new WorkoutEntry(UUID.randomUUID(), date, athlete, exercise, repetitions, minutes);
    }

    public String display() {
        return String.format(Locale.ROOT, "%s | %s | %-16s | %-18s | %4d reps | %5.1f min | %s",
                date, athlete, exercise, "Training", repetitions, minutes, id.toString().substring(0, 8));
    }
}
