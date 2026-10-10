package tgpr.docslot.view;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import tgpr.docslot.controller.HomeController;
import tgpr.docslot.model.SystemTime;
import tgpr.docslot.model.User;
import tgpr.framework.ui.layout.BorderPane;
import tgpr.framework.ui.layout.Insets;
import tgpr.framework.ui.layout.Layout;
import tgpr.framework.ui.layout.Pos;
import tgpr.framework.ui.layout.Spacing;
import tgpr.framework.ui.layout.VBox;

import java.util.List;

import static tgpr.framework.util.Tools.asAppointmentDateTime;

public class HomeView extends BasicWindow {

    private final HomeController controller;

    public HomeView(HomeController controller) {
        this.controller = controller;
        setTitle(titleWithUser());
        setHints(List.of(Hint.EXPANDED));

        setComponent(BorderPane.create()
                .spacing(Spacing.of(0, 1))
                .padding(Insets.of(1, 0, 1, 0))
                .centerAlign(Pos.TOP_CENTER)
                .top(createMenu())
                .center(createCenter()));
    }

    private String titleWithUser() {
        User user = controller.getLoggedUser();
        String email = user == null ? "?" : user.getEmail();
        return "DocSlot — Accueil [" + email + " — " + clockLabel() + "]";
    }

    private static String clockLabel() {
        var simulated = SystemTime.getSimulatedAt();
        if (simulated != null)
            return asAppointmentDateTime(simulated);
        return "date/heure système";
    }

    private MenuBar createMenu() {
        var menuApp = new Menu("App");
        menuApp.add(new MenuItem("Temps simulé", this::openSimulatedTime));
        menuApp.add(new MenuItem("Déconnexion", controller::logout));

        var bar = new MenuBar();
        bar.add(menuApp);
        return bar;
    }

    private void openSimulatedTime() {
        controller.openSimulatedTime();
        setTitle(titleWithUser());
    }

    private Panel createCenter() {
        return VBox.createCentered().spacing(1).add(
                Layout.bordered(" Session ",
                        VBox.create().spacing(1).add(
                                new Label("Connecté en tant que : " + controller.getDisplayName()),
                                new Label("Rôle : " + controller.getRoleLabel()),
                                new Label("E-mail : " + controller.getLoggedUser().getEmail())
                        )),
                new Label("Écran temporaire — à remplacer par les accueils métier."),
                new Button("Déconnexion", controller::logout)
        );
    }
}
