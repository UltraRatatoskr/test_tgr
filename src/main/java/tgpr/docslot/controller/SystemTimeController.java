package tgpr.docslot.controller;

import tgpr.docslot.model.SystemTime;
import tgpr.docslot.view.SystemTimeView;
import tgpr.framework.mvc.Controller;
import tgpr.framework.validation.Error;
import tgpr.framework.validation.ErrorList;

import java.time.LocalDateTime;

public class SystemTimeController extends Controller<SystemTimeView> {

    private final SystemTimeView view;

    public SystemTimeController() {
        view = new SystemTimeView(this);
    }

    @Override
    public SystemTimeView getView() {
        return view;
    }

    public LocalDateTime getCurrentReference() {
        return SystemTime.now();
    }

    public boolean isUsingSystemClock() {
        return SystemTime.isUsingSystemClock();
    }

    public String format(LocalDateTime value) {
        return SystemTime.formatInput(value);
    }

    public LocalDateTime parseOrNow(String dateTimeText) {
        return SystemTime.parseInputOrNow(dateTimeText);
    }

    public ErrorList validateDateTime(String dateTimeText) {
        var errors = new ErrorList();
        if (dateTimeText == null || dateTimeText.isBlank())
            errors.add(new Error("date/heure requise", SystemTime.Fields.SimulatedAt));
        else if (!SystemTime.tryParseInput(dateTimeText))
            errors.add(new Error("date invalide ou format incorrect", SystemTime.Fields.SimulatedAt));
        return errors;
    }

    public ErrorList save(boolean useSystemClock, String dateTimeText) {
        if (useSystemClock) {
            if (!askConfirmation(
                    "Vider le temps simulé et utiliser l’horloge système ?",
                    "Horloge système"))
                return new ErrorList();
            SystemTime.clear();
            view.close();
            return new ErrorList();
        }

        var errors = validateDateTime(dateTimeText);
        if (!errors.isEmpty())
            return errors;

        SystemTime.setSimulatedAt(SystemTime.parseInput(dateTimeText));
        view.close();
        return errors;
    }

    public void close() {
        view.close();
    }
}
