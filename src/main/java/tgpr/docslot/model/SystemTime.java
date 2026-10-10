package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SystemTime extends Model {

    public enum Fields {
        SimulatedAt
    }

    public static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private SystemTime() {
    }

    public static LocalDateTime getSimulatedAt() {
        return queryScalar(LocalDateTime.class, "select simulated_at from system_time", new Params());
    }

    public static void setSimulatedAt(LocalDateTime simulatedAt) {
        int c = execute("update system_time set simulated_at=:simulatedAt",
                new Params("simulatedAt", simulatedAt));
        Assert.isTrue(c == 1, "Something went wrong");
    }

    public static void clear() {
        setSimulatedAt(null);
    }

    public static boolean isUsingSystemClock() {
        return getSimulatedAt() == null;
    }


    public static LocalDateTime now() {
        LocalDateTime simulatedAt = getSimulatedAt();
        return simulatedAt != null ? simulatedAt : LocalDateTime.now();
    }

    public static String formatInput(LocalDateTime value) {
        return value == null ? "" : value.format(INPUT_FORMAT);
    }

    public static LocalDateTime parseInput(String text) {
        return LocalDateTime.parse(text.trim(), INPUT_FORMAT);
    }

    public static LocalDateTime parseInputOrNow(String text) {
        if (text == null || text.isBlank())
            return LocalDateTime.now();
        return parseInput(text);
    }

    public static boolean tryParseInput(String text) {
        try {
            parseInput(text);
            return true;
        } catch (DateTimeParseException | NullPointerException ex) {
            return false;
        }
    }

    @Override
    protected void mapper(ResultSet rs) {

    }

    @Override
    public void reload() {

    }
}
