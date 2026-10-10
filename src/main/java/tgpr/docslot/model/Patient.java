package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static tgpr.framework.util.Tools.ifNull;

public class Patient extends Model {
    public enum Fields {
        Id, UserId, LastName, FirstName, BirthDate, Phone, Email
    }

    private int id;
    private Integer userId;
    private String lastName;
    private String firstName;
    private LocalDate birthDate;
    private String phone;
    private String email;

    public Patient() {
    }

    public Patient(String lastName, String firstName, LocalDate birthDate, String phone, String email) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
    }

    public Patient(Integer userId, String lastName, String firstName, LocalDate birthDate, String phone, String email) {
        this(lastName, firstName, birthDate, phone, email);
        this.userId = userId;
    }

    public Patient(int id, Integer userId, String lastName, String firstName, LocalDate birthDate, String phone, String email) {
        this(userId, lastName, firstName, birthDate, phone, email);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        if (userId == null)
            return null;
        return User.getById(userId);
    }

    public List<Appointment> getAppointments() {
        return queryList(Appointment.class,
                "select * from appointment where patient_id=:patientId order by start_datetime",
                new Params("patientId", id));
    }

    public String getDisplayName() {
        return (ifNull(lastName, "") + " " + ifNull(firstName, "")).trim();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", userId=" + userId +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", birthDate=" + birthDate +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        // INT nullable : getInt renvoie 0 si SQL NULL → tester wasNull()
        int uid = rs.getInt("user_id");
        userId = rs.wasNull() ? null : uid;
        lastName = rs.getString("last_name");
        firstName = rs.getString("first_name");
        birthDate = rs.getObject("birth_date", LocalDate.class);
        phone = rs.getString("phone");
        email = rs.getString("email");
    }

    @Override
    public void reload() {
        reload("select * from patient where id=:id", new Params("id", id));
    }

    public static Patient getById(int id) {
        return queryOne(Patient.class,
                "select * from patient where id=:id",
                new Params("id", id));
    }

    public static Patient getByUserId(int userId) {
        return queryOne(Patient.class,
                "select * from patient where user_id=:userId",
                new Params("userId", userId));
    }

    public static List<Patient> getAll() {
        return queryList(Patient.class, "select * from patient order by last_name, first_name");
    }

    public Patient save() {
        if (id == 0) {
            int newId = insert(
                    "insert into patient (user_id, last_name, first_name, birth_date, phone, email) values (:userId,:lastName,:firstName,:birthDate,:phone,:email)",
                    new Params("userId", userId)
                            .add("lastName", lastName)
                            .add("firstName", firstName)
                            .add("birthDate", birthDate)
                            .add("phone", phone)
                            .add("email", email));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update patient set user_id=:userId, last_name=:lastName, first_name=:firstName, birth_date=:birthDate, phone=:phone, email=:email where id=:id",
                    new Params("id", id)
                            .add("userId", userId)
                            .add("lastName", lastName)
                            .add("firstName", firstName)
                            .add("birthDate", birthDate)
                            .add("phone", phone)
                            .add("email", email));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from patient where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
