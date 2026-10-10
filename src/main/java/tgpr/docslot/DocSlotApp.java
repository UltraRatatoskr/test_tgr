package tgpr.docslot;

import tgpr.docslot.controller.LoginController;
import tgpr.docslot.model.User;
import tgpr.framework.mvc.Controller;
import tgpr.framework.mvc.Model;
import tgpr.framework.util.Tools;

public class DocSlotApp {
    public static final String DATABASE_SCRIPT_FILE = "/database/tgpr-2627-a01.sql";

    public static void main(String[] args) {
        if (!Model.checkDb(DATABASE_SCRIPT_FILE))
            Controller.abort("La base de données n'est pas disponible !");
        else {

            /*
             * Exemple de navigation dans les données via le modèle
             */

            // récupère un utilisateur patient
            var user = User.getByEmail("marie.dupont@mail.be");
            // et ses rendez-vous
            var appointments = user.getPatient().getAppointments();
            // affiche les rendez-vous
            for (var a : appointments) {
                System.out.println("Rendez-vous #" + a.getId() + " : le " + Tools.asString(a.getStartDatetime()) +
                        " avec " + a.getDoctor().getDisplayName() +
                        " (" + a.getStatus() + ").");
            }

            Controller.navigateTo(new LoginController());
        }
    }
}
