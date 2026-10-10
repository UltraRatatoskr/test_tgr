package tgpr.docslot.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import tgpr.docslot.controller.LoginController;
import tgpr.docslot.model.User;
import tgpr.framework.ui.Ui;
import tgpr.framework.ui.layout.BorderPane;
import tgpr.framework.ui.layout.FormGrid;
import tgpr.framework.ui.layout.GridPane;
import tgpr.framework.ui.layout.HBox;
import tgpr.framework.ui.layout.Insets;
import tgpr.framework.ui.layout.Layout;
import tgpr.framework.ui.layout.Pos;
import tgpr.framework.ui.layout.Spacing;
import tgpr.framework.ui.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class LoginView extends BasicWindow {

    private final LoginController controller;

    private final TextBox txtEmail = Ui.sizeTo(new TextBox(), 28);
    private final TextBox txtPassword = Ui.sizeTo(new TextBox().setMask('*'), 16);
    private final FormGrid formGrid;

    public LoginView(LoginController controller) {
        this.controller = controller;

        setTitle("Connexion — DocSlot");
        setHints(List.of(Hint.CENTERED));

        formGrid = FormGrid.create()
                .row("E-mail:", txtEmail, User.Fields.Email)
                .row("Mot de passe:", txtPassword, User.Fields.Password)
                .alignInParent(Pos.CENTER);

        txtEmail.setTextChangeListener((txt, byUser) -> {
            if (byUser)
                validateLive();
        });
        txtPassword.setTextChangeListener((txt, byUser) -> {
            if (byUser)
                validateLive();
        });

        setComponent(BorderPane.create()
                .spacing(Spacing.of(0, 1))
                .padding(Insets.of(1, 1, 1, 0))
                .centerAlign(Pos.TOP_CENTER)
                .center(VBox.createCentered().spacing(1).add(
                        formGrid,
                        createMainActions()
                ))
                .bottom(VBox.create().childrenAlign(Pos.CENTER).add(createDebugPanel())));

        txtEmail.takeFocus();
    }

    private Panel createMainActions() {
        var btnLogin = new Button("Connexion", this::login);
        var btnExit = new Button("Quitter", this::exit);

        Ui.addShortcut(this, btnLogin, KeyStroke.fromString("<A-c>"));
        Ui.addShortcut(this, btnExit, KeyStroke.fromString("<A-q>"));

        return HBox.create().add(btnLogin, btnExit).alignInParent(Pos.CENTER);
    }

    private Border createDebugPanel() {
        Button btnSimulatedTime = new Button("Temps simulé", controller::openSimulatedTime);
        Button btnReset = new Button("Reset Database", this::seedData);
        var accounts = GridPane.create(4)
                .spacing(Spacing.of(1, 0))
                .addSequential(debugRoleCells("Patient",
                        debugLoginButton("Dupont Marie", "patient"),
                        debugLoginButton("Martin Sophie", "patient_sophie"),
                        debugLoginButton("Renard Inès", "patient_ines")))
                .addSequential(debugRoleCells("Médecin",
                        debugLoginButton("Leroy Antoine", "doctor"),
                        debugLoginButton("Kim Emma", "doctor_kim"),
                        debugLoginButton("Peeters Marc", "doctor_peeters")))
                .addSequential(debugRoleCells("Secrétaire",
                        debugLoginButton("Dupont Sophie", "secretary"),
                        debugLoginButton("Wauters Julie", "secretary_julie")))
                .addSequential(debugRoleCells("Admin",
                        debugLoginButton("DocSlot Admin", "admin")))
                .alignInParent(Pos.CENTER);

        var panel = VBox.createCentered()
                .padding(Insets.of(1))
                .spacing(0)
                .add(
                        accounts,
                        new EmptySpace(new TerminalSize(0, 1)),
                        HBox.create().add(btnSimulatedTime, btnReset).alignInParent(Pos.CENTER)
                );

        Ui.addShortcut(this, btnSimulatedTime, KeyStroke.fromString("<A-t>"));
        Ui.addShortcut(this, btnReset, KeyStroke.fromString("<A-r>"));

        return Layout.bordered(" Debug — login ", panel);
    }


    private Component[] debugRoleCells(String roleLabel, Button... buttons) {
        var cells = new ArrayList<Component>(4);
        cells.add(new Label(roleLabel));
        for (int i = 0; i < 3; i++) {
            cells.add(i < buttons.length ? buttons[i] : new EmptySpace());
        }
        return cells.toArray(Component[]::new);
    }

    private void validateLive() {
        formGrid.applyErrors(controller.validate(txtEmail.getText(), txtPassword.getText()));
    }

    private void login() {
        var errors = controller.login(txtEmail.getText(), txtPassword.getText());
        formGrid.applyErrors(errors);
        if (!errors.isEmpty()) {
            txtEmail.takeFocus();
        }
    }

    private void exit() {
        if (LoginController.askConfirmation("Quitter l’application DocSlot ?", "Quitter")) {
            controller.exit();
        }
    }

    private void seedData() {
        if (LoginController.askConfirmation(
                "Réinitialiser la base de données ? Toutes les données seront perdues.",
                "Reset Database")) {
            controller.seedData();
        }
    }

    private Button debugLoginButton(String name, String configKey) {
        return new Button(name, () -> controller.debugLogin(configKey));
    }
}
