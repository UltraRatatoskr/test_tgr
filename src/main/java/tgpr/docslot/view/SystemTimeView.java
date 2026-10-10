package tgpr.docslot.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import tgpr.docslot.controller.SystemTimeController;
import tgpr.docslot.model.SystemTime;
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
import tgpr.framework.validation.Error;
import tgpr.framework.validation.ErrorList;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static tgpr.framework.util.Tools.asAppointmentDateTime;

public class SystemTimeView extends DialogWindow {

    private static final int DATETIME_COLUMNS = 17;

    private final SystemTimeController controller;

    private final Label lblEtat = new Label("");
    private final CheckBox chkSystemClock = new CheckBox("Utiliser l’horloge du PC");
    private final TextBox txtDateTime = Ui.sizeTo(new TextBox(), DATETIME_COLUMNS);
    private final FormGrid formGrid;

    public SystemTimeView(SystemTimeController controller) {
        super("Temps simulé");
        this.controller = controller;

        setHints(List.of(Hint.CENTERED));
        setCloseWindowWithEscape(true);

        boolean systemClock = controller.isUsingSystemClock();
        chkSystemClock.setChecked(systemClock);
        txtDateTime.setText(controller.format(controller.getCurrentReference()));
        txtDateTime.setEnabled(!systemClock);

        formGrid = FormGrid.create()
                .row("Mode machine:", chkSystemClock)
                .addEmpty()
                .addEmpty()
                .row("Nouvelle date/heure:", HBox.create().add(
                        txtDateTime,
                        new Label("jj/mm/aaaa hh:mm")
                ), SystemTime.Fields.SimulatedAt);

        txtDateTime.setTextChangeListener((txt, byUser) -> {
            if (byUser)
                validateDateTimeField();
        });

        chkSystemClock.addListener(checked -> {
            txtDateTime.setEnabled(!checked);
            if (checked)
                txtDateTime.setText(controller.format(LocalDateTime.now()));
            clearFieldError();
        });

        refreshState();

        setComponent(BorderPane.create()
                .spacing(Spacing.of(0, 1))
                .padding(Insets.of(1, 1, 1, 0))
                .centerAlign(Pos.TOP_CENTER)
                .center(VBox.createCentered().spacing(0).add(
                        createStatePanel(),
                        new EmptySpace(new TerminalSize(0, 1)),
                        formGrid,
                        createShortcutButtons()
                ))
                .bottom(createActionButtons()));

        txtDateTime.takeFocus();
    }

    private Border createStatePanel() {
        return Layout.bordered(" État courant ", FormGrid.createPadded()
                .row("Référence:", lblEtat)
        );
    }

    private Panel createShortcutButtons() {
        return GridPane.create(2)
                .spacing(Spacing.of(1))
                .add(new Button("-1 jour", () -> shiftDays(-1)), 0, 0)
                .add(new Button("+1 jour", () -> shiftDays(1)), 1, 0)
                .add(new Button("-1 semaine", () -> shiftDays(-7)), 0, 1)
                .add(new Button("+1 semaine", () -> shiftDays(7)), 1, 1)
                .add(new Button("Maintenant", this::setNowInField), 0, 2, 2, 1, Pos.CENTER)
                .alignInParent(Pos.CENTER);
    }

    private Panel createActionButtons() {
        return VBox.create()
                .childrenAlign(Pos.CENTER)
                .add(HBox.create().spacing(2).add(
                        new Button("Enregistrer", this::save),
                        new Button("Retour", controller::close)
                ));
    }

    private void refreshState() {
        lblEtat.setText(asAppointmentDateTime(controller.getCurrentReference()));
    }

    private void shiftDays(int days) {
        chkSystemClock.setChecked(false);
        txtDateTime.setEnabled(true);
        try {
            var value = controller.parseOrNow(txtDateTime.getText()).plusDays(days);
            txtDateTime.setText(controller.format(value));
            clearFieldError();
        } catch (DateTimeParseException ex) {
            showFieldError("date invalide ou format incorrect");
        }
    }

    private void setNowInField() {
        chkSystemClock.setChecked(false);
        txtDateTime.setEnabled(true);
        txtDateTime.setText(controller.format(LocalDateTime.now()));
        clearFieldError();
    }

    private void save() {
        if (!validateDateTimeField() && !chkSystemClock.isChecked()) {
            txtDateTime.takeFocus();
            return;
        }
        var errors = controller.save(chkSystemClock.isChecked(), txtDateTime.getText());
        if (!errors.isEmpty()) {
            formGrid.applyErrors(errors);
            txtDateTime.takeFocus();
        }
    }

    private boolean validateDateTimeField() {
        if (chkSystemClock.isChecked()) {
            clearFieldError();
            return true;
        }
        var errors = controller.validateDateTime(txtDateTime.getText());
        formGrid.applyErrors(errors);
        return errors.isEmpty();
    }

    private void clearFieldError() {
        formGrid.applyErrors(new ErrorList());
    }

    private void showFieldError(String message) {
        var errors = new ErrorList();
        errors.add(new Error(message, SystemTime.Fields.SimulatedAt));
        formGrid.applyErrors(errors);
        txtDateTime.takeFocus();
    }
}
