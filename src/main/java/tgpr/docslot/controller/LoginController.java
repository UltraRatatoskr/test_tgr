package tgpr.docslot.controller;

import tgpr.docslot.DocSlotApp;
import tgpr.docslot.model.Security;
import tgpr.docslot.model.User;
import tgpr.docslot.view.LoginView;
import tgpr.framework.mvc.Controller;
import tgpr.framework.mvc.Model;
import tgpr.framework.util.Configuration;
import tgpr.framework.validation.Error;
import tgpr.framework.validation.ErrorList;

public class LoginController extends Controller<LoginView> {

    @Override
    public LoginView getView() {
        return new LoginView(this);
    }

    public void exit() {
        System.exit(0);
    }

    public ErrorList login(String email, String password) {
        var errors = validate(email, password);

        if (errors.isEmpty()) {
            var user = User.checkCredentials(email.trim(), password);
            if (user != null) {
                Security.login(user);
                navigateTo(new HomeController());
            } else {
                errors.add(new Error("identifiants incorrects ou compte désactivé", User.Fields.Email));
            }
        }

        return errors;
    }


    public ErrorList validate(String email, String password) {
        var errors = new ErrorList();
        if (email == null || email.isBlank())
            errors.add(new Error("e-mail requis", User.Fields.Email));
        if (password == null || password.isBlank())
            errors.add(new Error("mot de passe requis", User.Fields.Password));
        return errors;
    }


    public void debugLogin(String configKey) {
        var user = User.checkCredentials(
                Configuration.get("default." + configKey + ".email"),
                Configuration.get("default." + configKey + ".password"));
        if (user != null) {
            Security.login(user);
            navigateTo(new HomeController());
        } else {
            showError(new Error("Compte debug introuvable ou inactif : default." + configKey + ".*"));
        }
    }


    public void openSimulatedTime() {
        navigateTo(new SystemTimeController());
    }

    public void seedData() {
        Model.seedData(DocSlotApp.DATABASE_SCRIPT_FILE);
    }
}
