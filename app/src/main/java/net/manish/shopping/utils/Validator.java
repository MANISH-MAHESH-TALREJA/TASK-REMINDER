package net.manish.shopping.utils;

public class Validator {

    public static boolean isValidPassword(String pass){
        String passwordPattern = "^(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$";
        return pass.matches(passwordPattern);
    }

    public static boolean isValidPhone(String phone){
        return phone.matches(phone);
    }
}
