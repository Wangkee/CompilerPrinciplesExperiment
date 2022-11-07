package Exp01;

public class Token {
    private String value;
    private String type;
    private int code;

    public Token(String value, String type, int code) {
        this.value = value;
        this.type = type;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", code=" + code +
                '}';
    }
}
