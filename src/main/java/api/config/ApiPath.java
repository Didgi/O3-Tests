package api.config;

public class ApiPath {
    public final static String BASE_URI = Config.getProperty("api_baseurl");
    public final static String GET_SESSION_PATH = "/session";
    public final static String CHANGE_PASSWORD_BY_ADMIN_PATH = "/password/:{userUuid}";
    public final static String CHANGE_PASSWORD_BY_USER_PATH = "/password";
}
