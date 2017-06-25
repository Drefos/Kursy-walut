package app;


public class Currency{
    private String country;
    private String code;
    private String value;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Currency(String country, String code, String value) {
        setCountry(country);
        setCode(code);
        setValue(value);
    }

    public Currency() {

    }

}
