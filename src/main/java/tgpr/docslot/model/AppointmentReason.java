package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class AppointmentReason extends Model {
    public enum Fields {
        Id, Name, DurationMinutes, BookableOnline
    }

    private int id;
    private String name;
    private int durationMinutes;
    private boolean bookableOnline;

    public AppointmentReason() {
    }

    public AppointmentReason(String name, int durationMinutes, boolean bookableOnline) {
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.bookableOnline = bookableOnline;
    }

    public AppointmentReason(int id, String name, int durationMinutes, boolean bookableOnline) {
        this(name, durationMinutes, bookableOnline);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isBookableOnline() {
        return bookableOnline;
    }

    public void setBookableOnline(boolean bookableOnline) {
        this.bookableOnline = bookableOnline;
    }

    public List<Appointment> getAppointments() {
        return queryList(Appointment.class,
                "select * from appointment where reason_id=:reasonId order by start_datetime",
                new Params("reasonId", id));
    }

    @Override
    public String toString() {
        return "AppointmentReason{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", bookableOnline=" + bookableOnline +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppointmentReason that = (AppointmentReason) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        name = rs.getString("name");
        durationMinutes = rs.getInt("duration_minutes");
        bookableOnline = rs.getBoolean("bookable_online");
    }

    @Override
    public void reload() {
        reload("select * from appointment_reason where id=:id", new Params("id", id));
    }

    public static AppointmentReason getById(int id) {
        return queryOne(AppointmentReason.class,
                "select * from appointment_reason where id=:id",
                new Params("id", id));
    }

    public static AppointmentReason getByName(String name) {
        return queryOne(AppointmentReason.class,
                "select * from appointment_reason where name=:name",
                new Params("name", name));
    }

    public static List<AppointmentReason> getAll() {
        return queryList(AppointmentReason.class, "select * from appointment_reason order by name");
    }

    public AppointmentReason save() {
        if (id == 0) {
            int newId = insert(
                    "insert into appointment_reason (name, duration_minutes, bookable_online) values (:name,:durationMinutes,:bookableOnline)",
                    new Params("name", name)
                            .add("durationMinutes", durationMinutes)
                            .add("bookableOnline", bookableOnline));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update appointment_reason set name=:name, duration_minutes=:durationMinutes, bookable_online=:bookableOnline where id=:id",
                    new Params("id", id)
                            .add("name", name)
                            .add("durationMinutes", durationMinutes)
                            .add("bookableOnline", bookableOnline));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from appointment_reason where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
