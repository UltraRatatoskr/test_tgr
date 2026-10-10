package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static tgpr.framework.util.Tools.ifNull;

public class Doctor extends Model {
    public enum Fields {
        Id, UserId, LastName, FirstName, InamiNumber, Active
    }

    private int id;
    private int userId;
    private String lastName;
    private String firstName;
    private String inamiNumber;
    private boolean active;

    public Doctor() {
    }

    public Doctor(int userId, String lastName, String firstName, String inamiNumber, boolean active) {
        this.userId = userId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.inamiNumber = inamiNumber;
        this.active = active;
    }

    public Doctor(int id, int userId, String lastName, String firstName, String inamiNumber, boolean active) {
        this(userId, lastName, firstName, inamiNumber, active);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInamiNumber() {
        return inamiNumber;
    }

    public void setInamiNumber(String inamiNumber) {
        this.inamiNumber = inamiNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getUser() {
        return User.getById(userId);
    }

    public List<Specialty> getSpecialties() {
        return queryList(Specialty.class,
                "select s.* from doctor_specialty ds join specialty s on ds.specialty_id = s.id where ds.doctor_id=:doctorId order by s.name",
                new Params("doctorId", id));
    }

    public void addSpecialty(Specialty specialty) {
        if (specialty == null)
            return;
        if (!getSpecialties().contains(specialty)) {
            int c = execute(
                    "insert into doctor_specialty (doctor_id, specialty_id) values (:doctorId,:specialtyId)",
                    new Params("doctorId", id).add("specialtyId", specialty.getId()));
            Assert.isTrue(c == 1, "Something went wrong");
        }
    }

    public void removeSpecialty(Specialty specialty) {
        if (specialty == null)
            return;
        int c = execute(
                "delete from doctor_specialty where doctor_id=:doctorId and specialty_id=:specialtyId",
                new Params("doctorId", id).add("specialtyId", specialty.getId()));
        Assert.isTrue(c == 1, "Something went wrong");
    }

    public List<OnlineBookingSlot> getOnlineBookingSlots() {
        return queryList(OnlineBookingSlot.class,
                "select * from online_booking_slot where doctor_id=:doctorId order by start_datetime",
                new Params("doctorId", id));
    }

    public List<Appointment> getAppointments() {
        return queryList(Appointment.class,
                "select * from appointment where doctor_id=:doctorId order by start_datetime",
                new Params("doctorId", id));
    }

    public String getDisplayName() {
        String name = (ifNull(firstName, "") + " " + ifNull(lastName, "")).trim();
        return name.isEmpty() ? "Dr" : "Dr " + name;
    }

    public String getSpecialtiesLabel() {
        return getSpecialties().stream().map(Specialty::getName).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", userId=" + userId +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", inamiNumber='" + inamiNumber + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        userId = rs.getInt("user_id");
        lastName = rs.getString("last_name");
        firstName = rs.getString("first_name");
        inamiNumber = rs.getString("inami_number");
        active = rs.getBoolean("active");
    }

    @Override
    public void reload() {
        reload("select * from doctor where id=:id", new Params("id", id));
    }

    public static Doctor getById(int id) {
        return queryOne(Doctor.class,
                "select * from doctor where id=:id",
                new Params("id", id));
    }

    public static Doctor getByUserId(int userId) {
        return queryOne(Doctor.class,
                "select * from doctor where user_id=:userId",
                new Params("userId", userId));
    }

    public static Doctor getByInami(String inamiNumber) {
        return queryOne(Doctor.class,
                "select * from doctor where inami_number=:inamiNumber",
                new Params("inamiNumber", inamiNumber));
    }

    public static List<Doctor> getAll() {
        return queryList(Doctor.class, "select * from doctor order by last_name, first_name");
    }

    public Doctor save() {
        if (id == 0) {
            int newId = insert(
                    "insert into doctor (user_id, last_name, first_name, inami_number, active) values (:userId,:lastName,:firstName,:inamiNumber,:active)",
                    new Params("userId", userId)
                            .add("lastName", lastName)
                            .add("firstName", firstName)
                            .add("inamiNumber", inamiNumber)
                            .add("active", active));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update doctor set user_id=:userId, last_name=:lastName, first_name=:firstName, inami_number=:inamiNumber, active=:active where id=:id",
                    new Params("id", id)
                            .add("userId", userId)
                            .add("lastName", lastName)
                            .add("firstName", firstName)
                            .add("inamiNumber", inamiNumber)
                            .add("active", active));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from doctor where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
