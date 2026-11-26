package controllers;

public class LoginController {

    public boolean checkNameAndUserPassword(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String cleanUsername = username.trim();
        return ("Dinujaya".equals(cleanUsername) || "admin".equalsIgnoreCase(cleanUsername))
                && "1234".equals(password);
    }
}