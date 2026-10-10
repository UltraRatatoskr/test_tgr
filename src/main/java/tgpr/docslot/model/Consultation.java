package tgpr.docslot.model;

import org.springframework.util.Assert;
import tgpr.framework.mvc.Model;
import tgpr.framework.mvc.Params;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Consultation extends Model {
    public enum Fields {
        Id, AppointmentId, Report, Diagnosis, Treatment, PatientInstructions, VisibleToPatient
    }

    private int id;
    private int appointmentId;
    private String report;
    private String diagnosis;
    private String treatment;
    private String patientInstructions;
    private boolean visibleToPatient;

    public Consultation() {
    }

    public Consultation(int appointmentId, String report, String diagnosis, String treatment,
                        String patientInstructions, boolean visibleToPatient) {
        this.appointmentId = appointmentId;
        this.report = report;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.patientInstructions = patientInstructions;
        this.visibleToPatient = visibleToPatient;
    }

    public Consultation(int id, int appointmentId, String report, String diagnosis, String treatment,
                        String patientInstructions, boolean visibleToPatient) {
        this(appointmentId, report, diagnosis, treatment, patientInstructions, visibleToPatient);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPatientInstructions() {
        return patientInstructions;
    }

    public void setPatientInstructions(String patientInstructions) {
        this.patientInstructions = patientInstructions;
    }

    public boolean isVisibleToPatient() {
        return visibleToPatient;
    }

    public void setVisibleToPatient(boolean visibleToPatient) {
        this.visibleToPatient = visibleToPatient;
    }

    public Appointment getAppointment() {
        return Appointment.getById(appointmentId);
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", appointmentId=" + appointmentId +
                ", report='" + report + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                ", patientInstructions='" + patientInstructions + '\'' +
                ", visibleToPatient=" + visibleToPatient +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consultation that = (Consultation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void mapper(ResultSet rs) throws SQLException {
        id = rs.getInt("id");
        appointmentId = rs.getInt("appointment_id");
        report = rs.getString("report");
        diagnosis = rs.getString("diagnosis");
        treatment = rs.getString("treatment");
        patientInstructions = rs.getString("patient_instructions");
        visibleToPatient = rs.getBoolean("visible_to_patient");
    }

    @Override
    public void reload() {
        reload("select * from consultation where id=:id", new Params("id", id));
    }

    public static Consultation getById(int id) {
        return queryOne(Consultation.class,
                "select * from consultation where id=:id",
                new Params("id", id));
    }

    public static Consultation getByAppointmentId(int appointmentId) {
        return queryOne(Consultation.class,
                "select * from consultation where appointment_id=:appointmentId",
                new Params("appointmentId", appointmentId));
    }

    public static List<Consultation> getAll() {
        return queryList(Consultation.class, "select * from consultation order by id");
    }

    public Consultation save() {
        if (id == 0) {
            int newId = insert(
                    "insert into consultation (appointment_id, report, diagnosis, treatment, patient_instructions, visible_to_patient) values (:appointmentId,:report,:diagnosis,:treatment,:patientInstructions,:visibleToPatient)",
                    new Params("appointmentId", appointmentId)
                            .add("report", report)
                            .add("diagnosis", diagnosis)
                            .add("treatment", treatment)
                            .add("patientInstructions", patientInstructions)
                            .add("visibleToPatient", visibleToPatient));
            Assert.isTrue(newId > 0, "Something went wrong");
            setId(newId);
        } else {
            int c = execute(
                    "update consultation set appointment_id=:appointmentId, report=:report, diagnosis=:diagnosis, treatment=:treatment, patient_instructions=:patientInstructions, visible_to_patient=:visibleToPatient where id=:id",
                    new Params("id", id)
                            .add("appointmentId", appointmentId)
                            .add("report", report)
                            .add("diagnosis", diagnosis)
                            .add("treatment", treatment)
                            .add("patientInstructions", patientInstructions)
                            .add("visibleToPatient", visibleToPatient));
            Assert.isTrue(c == 1, "Something went wrong");
        }
        return this;
    }

    public void delete() {
        int c = execute("delete from consultation where id=:id", new Params("id", id));
        Assert.isTrue(c == 1, "Something went wrong");
    }
}
