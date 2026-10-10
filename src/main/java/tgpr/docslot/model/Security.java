package tgpr.docslot.model;

public abstract class Security {
    private static User loggedUser = null;

    public static User getLoggedUser() {
        return loggedUser;
    }

    public static void login(User user) {
        loggedUser = user;
    }

    public static boolean isLogged() {
        return loggedUser != null;
    }

    public static boolean isLoggedUser(User user) {
        return loggedUser != null && loggedUser.equals(user);
    }

    public static void logout() {
        login(null);
    }

    public static boolean isAdmin() {
        return loggedUser != null && loggedUser.getRole() == User.Role.ADMIN;
    }

    public static boolean isSecretary() {
        return loggedUser != null && loggedUser.getRole() == User.Role.SECRETARY;
    }

    public static boolean isDoctor() {
        return loggedUser != null && loggedUser.getRole() == User.Role.DOCTOR;
    }

    public static boolean isPatient() {
        return loggedUser != null && loggedUser.getRole() == User.Role.PATIENT;
    }
}
