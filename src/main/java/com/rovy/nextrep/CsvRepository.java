package com.rovy.nextrep;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Simple local persistence. Text fields are encoded as Base64 UTF-8 to avoid CSV escaping ambiguities. */
public final class CsvRepository {
    private static final String HEADER = "id,date,athlete_b64,exercise_b64,repetitions,minutes";
    private CsvRepository() {}

    public static void save(Path path, List<WorkoutEntry> entries) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (WorkoutEntry e : entries) lines.add(String.join(",", e.id().toString(), e.date().toString(),
                encode(e.athlete()), encode(e.exercise()), Integer.toString(e.repetitions()),
                Double.toString(e.minutes())));
        Path tmp = Files.createTempFile(parent, "nextrep-", ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }

    public static List<WorkoutEntry> load(Path path) throws IOException {
        if (!Files.exists(path)) return List.of();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) throw new IOException("invalid data header");
        List<WorkoutEntry> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",", -1);
            try {
                if (values.length != 6) throw new IllegalArgumentException("expected six columns");
                result.add(new WorkoutEntry(UUID.fromString(values[0]), LocalDate.parse(values[1]),
                        decode(values[2]), decode(values[3]), Integer.parseInt(values[4]),
                        Double.parseDouble(values[5])));
            } catch (RuntimeException ex) { throw new IOException("invalid record at line " + (i + 1), ex); }
        }
        return List.copyOf(result);
    }

    private static String encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
    private static String decode(String s) {
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
