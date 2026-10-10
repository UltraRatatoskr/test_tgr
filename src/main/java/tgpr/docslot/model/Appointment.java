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

public class Appointment extends Model {
    public enum Fields {
        Id, PatientId, DoctorId, ReasonId, SpecialtyId, StartDatetime, EndDatetime, Status, Origin, CreatedByUserId
    }

    public enum Status {
        REQUESTED, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }

    public enum Origin {
        INTERNAL, ONLINE
    }

    private int id;
    private int patientId;
    private int doctorId;
    private int reasonId;
    private int specialtyId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Status status;
    private Origin origin;
    private Integer createdByUserId;

    public Appointment() {
    }

    public Appointment(int patientId, int doctorId, int reasonId, int specialtyId,
                       LocalDateTime startDatetime, LocalDateTime endDatetime,
                       Status status, Origin origin, Integer createdByUserId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.reasonId = reasonId;
        this.specialtyId = specialtyId;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.status = status;
        this.origin = origin;
        this.createdByUserId = createdByUserId;
    }

    public Appointment(int id, int patientId, int doctorId, int reasonId, int specialtyId,
                       LocalDateTime startDatetime, LocalDateTime endDatetime,
                       Status status, Origin origin, Integer createdByUserId) {
        this(patientId, doctorId, reasonId, specialtyId, startDatetime, endDatetime, status, origin, createdByUserId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(int specialtyId) {
        this.specialtyId = specialtyId;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Patient getPatient() {
        return Patient.getById(patientId);
    }

    public Doctor getDoctor() {
        return Doctor.getById(doctorId);
    }

    public AppointmentReason getReason() {
        return AppointmentReason.getById(reasonId);
    }

    public Specialty getSpecialty() {
        return Specialty.getById(specialtyId);
    }

    public User getCreatedByUser() {
        return createdByUserId == null ? null : User.getById(createdByUserId);
    }

    public Consultation getConsultation() {
        return Consultation.getByAppointmentId(id);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", reasonId=" + reasonId +
                ", specialtyId=" + specialtyId +
                ", startDatetime=" + asString(startDatetime) +
                ", endDatetime=" + asString(endDatetime) +
                ", status=" + status +
                ", origin=" + origin +
                ", createdByUserId=" + createdByUserId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        patientId = rs.getInt("patient_id");
        doctorId = rs.getInt("doctor_id");
        reasonId = rs.getInt("reason_id");
        specialtyId = rs.getInt("specialty_id");
        startDatetime = rs.getObject("start_datetime", LocalDateTime.class);
        endDatetime = rs.getObject("end_datetime", LocalDateTime.class);
        status = Status.valueOf(rs.getString("status"));
        origin = Origin.valueOf(rs.getString("origin"));
        // INT nullable : getInt renvoie 0 si SQL NULL → tester wasNull()
        int cuid = rs.getInt("created_by_user_id");
        createdByUserId = rs.wasNull() ? null : cuid;
    }

    @Override
    public void reload() {
        reload("select * from appointment where id=:id", new Params("id", id));
    }

    public static Appointment getById(int id) {
        return queryOne(Appointment.class,
                "select * from appointment where id=:id",
                new Params("id", id));
    }

    public static List<Appointment> getAll() {
        return queryList(Appointment.class,
                "select * from appointment order by start_datetime");
    }

    public Appointment save() {
        if (id == 0) {
            int newId = insert(
                    "insert into appointment (patient_id, doctor_id, reason_id, specialty_id, start_datetime, end_datetime, status, origin, created_by_user_id) values (:patientId,:doctorId,:reasonId,:specialtyId,:startDatetime,:endDatetime,:status,:origin,:createdByUserId)",
                    new Params("patientId", patientId)
                            .add("doctorId", doctorId)
                            .add("reasonId", reasonId)
                            .add("specialtyId", specialtyId)
                            .add("startDatetime", startDatetime)
                            .add("endDatetime", endDatetime)
                            .add("status", status.name())
                            .add("origin", origin.name())
                            .add("createdByUserId", createdByUserId));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update appointment set patient_id=:patientId, doctor_id=:doctorId, reason_id=:reasonId, specialty_id=:specialtyId, start_datetime=:startDatetime, end_datetime=:endDatetime, status=:status, origin=:origin, created_by_user_id=:createdByUserId where id=:id",
                    new Params("id", id)
                            .add("patientId", patientId)
                            .add("doctorId", doctorId)
                            .add("reasonId", reasonId)
                            .add("specialtyId", specialtyId)
                            .add("startDatetime", startDatetime)
                            .add("endDatetime", endDatetime)
                            .add("status", status.name())
                            .add("origin", origin.name())
                            .add("createdByUserId", createdByUserId));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from appointment where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
