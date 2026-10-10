package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static tgpr.framework.util.Tools.hash;

public class User extends Model {
    public enum Fields {
        Id, Email, Password, PasswordConfirmation, PasswordHash, Role, Active
    }

    public enum Role {
        PATIENT, DOCTOR, SECRETARY, ADMIN
    }

    private int id;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;

    public User() {
    }

    public User(String email, String passwordHash, Role role, boolean active) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public User(int id, String email, String passwordHash, Role role, boolean active) {
        this(email, passwordHash, role, active);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Patient getPatient() {
        return queryOne(Patient.class,
                "select * from patient where user_id=:userId",
                new Params("userId", id));
    }

    public Doctor getDoctor() {
        return queryOne(Doctor.class,
                "select * from doctor where user_id=:userId",
                new Params("userId", id));
    }

    public List<Appointment> getCreatedAppointments() {
        return queryList(Appointment.class,
                "select * from appointment where created_by_user_id=:userId order by start_datetime",
                new Params("userId", id));
    }

    public String getDisplayName() {
        return switch (role) {
            case PATIENT -> {
                var patient = getPatient();
                yield patient != null
                        ? patient.getFirstName() + " " + patient.getLastName()
                        : email;
            }
            case DOCTOR -> {
                var doctor = getDoctor();
                yield doctor != null
                        ? "Dr " + doctor.getFirstName() + " " + doctor.getLastName()
                        : email;
            }
            case SECRETARY, ADMIN -> email;
        };
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        email = rs.getString("email");
        passwordHash = rs.getString("password_hash");
        role = Role.valueOf(rs.getString("role"));
        active = rs.getBoolean("active");
    }

    @Override
    public void reload() {
        reload("select * from `user` where id=:id", new Params("id", id));
    }

    public static User getById(int id) {
        return queryOne(User.class,
                "select * from `user` where id=:id",
                new Params("id", id));
    }

    public static User getByEmail(String email) {
        return queryOne(User.class,
                "select * from `user` where email=:email",
                new Params("email", email));
    }

    public static User checkCredentials(String email, String password) {
        var user = getByEmail(email);
        if (user != null && user.isActive() && user.passwordHash.equals(hash(password)))
            return user;
        return null;
    }

    public static List<User> getAll() {
        return queryList(User.class, "select * from `user` order by email");
    }

    public User save() {
        if (id == 0) {
            int newId = insert(
                    "insert into `user` (email, password_hash, role, active) values (:email,:passwordHash,:role,:active)",
                    new Params("email", email)
                            .add("passwordHash", passwordHash)
                            .add("role", role.name())
                            .add("active", active));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update `user` set email=:email, password_hash=:passwordHash, role=:role, active=:active where id=:id",
                    new Params("id", id)
                            .add("email", email)
                            .add("passwordHash", passwordHash)
                            .add("role", role.name())
                            .add("active", active));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from `user` where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
