package api.utils;

public class RegexData {
    public static final String USERNAME_TEMPLATE = "^[A-Za-z0-9]{3,15}$";
    public static final String PASSWORD_TEMPLATE = "^[A-Z]{3}[a-z]{3}[0-9]{2}[%!&]{2}$";
    public static final String USER_TEMPLATE = "^USER$";
    public static final String START_DATE = "202[7-9]-0[1-9]-1[0-5]T1[0-6]:[0-5][0-9]:[0-5][0-9]";
    public static final String END_DATE = "202[7-9]-0[1-9]-1[0-5]T1[7-8]:[0-5][0-9]:[0-5][0-9]";
    public static final String NAME_TEMPLATE = "^[A-Z][a-z]{4,10}$";
    public static final String BIRTHDATE_TEMPLATE = "^19[7-9][0-9]-0[1-9]-[12][0-9]$";
    public static final String GENDER_TEMPLATE = "^[MF]$";
    public static final String ADDRESS_TEMPLATE = "^[0-9]{1,3} [A-Z][a-z]{4,10} Street$";
    public static final String CITY_TEMPLATE = "^[A-Z][a-z]{5,10}$";
    public static final String COUNTRY_TEMPLATE = "^[A-Z][a-z]{5,10}$";
    public static final String POSTAL_CODE_TEMPLATE = "^[0-9]{6}$";
    public static final String ROLE_NAME = "^Configures Forms";
    public static final String ROLE_DESCRIPTION = "^Manages forms and attaches them to the UI";
    public static final String USER_SYSTEM_ID = "^systemId[0-9]{5}$";
}
