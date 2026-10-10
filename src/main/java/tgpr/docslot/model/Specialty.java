package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Specialty extends Model {
    public enum Fields {
        Id, Name
    }

    private int id;
    private String name;

    public Specialty() {
    }

    public Specialty(String name) {
        this.name = name;
    }

    public Specialty(int id, String name) {
        this.id = id;
        this.name = name;
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

    public List<Doctor> getDoctors() {
        return queryList(Doctor.class,
                "select d.* from doctor_specialty ds join doctor d on ds.doctor_id = d.id where ds.specialty_id=:specialtyId order by d.last_name, d.first_name",
                new Params("specialtyId", id));
    }

    public List<Appointment> getAppointments() {
        return queryList(Appointment.class,
                "select * from appointment where specialty_id=:specialtyId order by start_datetime",
                new Params("specialtyId", id));
    }

    @Override
    public String toString() {
        return "Specialty{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Specialty specialty = (Specialty) o;
        return id == specialty.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        name = rs.getString("name");
    }

    @Override
    public void reload() {
        reload("select * from specialty where id=:id", new Params("id", id));
    }

    public static Specialty getById(int id) {
        return queryOne(Specialty.class,
                "select * from specialty where id=:id",
                new Params("id", id));
    }

    public static Specialty getByName(String name) {
        return queryOne(Specialty.class,
                "select * from specialty where name=:name",
                new Params("name", name));
    }

    public static List<Specialty> getAll() {
        return queryList(Specialty.class, "select * from specialty order by name");
    }

    public Specialty save() {
        if (id == 0) {
            int newId = insert(
                    "insert into specialty (name) values (:name)",
                    new Params("name", name));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update specialty set name=:name where id=:id",
                    new Params("id", id).add("name", name));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from specialty where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
