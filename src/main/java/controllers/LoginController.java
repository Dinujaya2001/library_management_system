package controllers;

public class LoginController {
    public boolean checkNameAndUserPassword(String username, String password) {
        if(username.equals("Dinujaya") && password.equals("1234")) {
            return true;
        }
        return false;
    }
}
