package api.utils;

public class RegexData {
    public static final String USERNAME_TEMPLATE = "^[A-Za-z0-9]{3,15}$";
    public static final String PASSWORD_TEMPLATE = "^[A-Z]{3}[a-z]{3}[0-9]{2}[%!&]{2}$";
    public static final String NAME_TEMPLATE = "^[A-Z]{5}\s[a-z]{5}$";
    public static final String USER_TEMPLATE = "^USER$";
}
