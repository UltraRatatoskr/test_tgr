package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static tgpr.framework.util.Tools.asString;

public class OnlineBookingSlot extends Model {
    public enum Fields {
        Id, DoctorId, StartDatetime, EndDatetime, Active
    }

    private int id;
    private int doctorId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private boolean active;

    public OnlineBookingSlot() {
    }

    public OnlineBookingSlot(int doctorId, LocalDateTime startDatetime, LocalDateTime endDatetime, boolean active) {
        this.doctorId = doctorId;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.active = active;
    }

    public OnlineBookingSlot(int id, int doctorId, LocalDateTime startDatetime, LocalDateTime endDatetime, boolean active) {
        this(doctorId, startDatetime, endDatetime, active);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Doctor getDoctor() {
        return Doctor.getById(doctorId);
    }

    @Override
    public String toString() {
        return "OnlineBookingSlot{" +
                "id=" + id +
                ", doctorId=" + doctorId +
                ", startDatetime=" + asString(startDatetime) +
                ", endDatetime=" + asString(endDatetime) +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineBookingSlot that = (OnlineBookingSlot) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        doctorId = rs.getInt("doctor_id");
        startDatetime = rs.getObject("start_datetime", LocalDateTime.class);
        endDatetime = rs.getObject("end_datetime", LocalDateTime.class);
        active = rs.getBoolean("active");
    }

    @Override
    public void reload() {
        reload("select * from online_booking_slot where id=:id", new Params("id", id));
    }

    public static OnlineBookingSlot getById(int id) {
        return queryOne(OnlineBookingSlot.class,
                "select * from online_booking_slot where id=:id",
                new Params("id", id));
    }

    public static List<OnlineBookingSlot> getAll() {
        return queryList(OnlineBookingSlot.class,
                "select * from online_booking_slot order by start_datetime");
    }

    public OnlineBookingSlot save() {
        if (id == 0) {
            int newId = insert(
                    "insert into online_booking_slot (doctor_id, start_datetime, end_datetime, active) values (:doctorId,:startDatetime,:endDatetime,:active)",
                    new Params("doctorId", doctorId)
                            .add("startDatetime", startDatetime)
                            .add("endDatetime", endDatetime)
                            .add("active", active));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update online_booking_slot set doctor_id=:doctorId, start_datetime=:startDatetime, end_datetime=:endDatetime, active=:active where id=:id",
                    new Params("id", id)
                            .add("doctorId", doctorId)
                            .add("startDatetime", startDatetime)
                            .add("endDatetime", endDatetime)
                            .add("active", active));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from online_booking_slot where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
