package tgpr.docslot.controller;

import tgpr.docslot.model.Security;
import tgpr.docslot.model.User;
import tgpr.docslot.view.HomeView;
import tgpr.framework.mvc.Controller;

public class HomeController extends Controller<HomeView> {

    private final HomeView view;

    public HomeController() {
        if (!Security.isLogged())
            throw new RuntimeException("Vous devez être connecté.");
        view = new HomeView(this);
    }

    @Override
    public HomeView getView() {
        return view;
    }

    public User getLoggedUser() {
        return Security.getLoggedUser();
    }

    public String getDisplayName() {
        User user = Security.getLoggedUser();
        return user == null ? "" : user.getDisplayName();
    }

    public String getRoleLabel() {
        User user = Security.getLoggedUser();
        if (user == null || user.getRole() == null)
            return "";
        return switch (user.getRole()) {
            case ADMIN -> "Administrateur";
            case SECRETARY -> "Secrétaire";
            case DOCTOR -> "Médecin";
            case PATIENT -> "Patient";
        };
    }

    public void openSimulatedTime() {
        navigateTo(new SystemTimeController());
    }

    public void logout() {
        Security.logout();
        navigateTo(new LoginController());
    }
}
