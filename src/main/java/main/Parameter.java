package main;

public class Parameter {

    private String name;
    private String value;

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Parameter(String nameValuePair) {
        if (nameValuePair.indexOf("=") == nameValuePair.lastIndexOf("=")) {
            String[] pair = nameValuePair.split("=");

            if(pair[0].isEmpty()) {
                throw new IllegalArgumentException("Parameter name cannot be empty!");
            }

            this.name = pair[0];
            this.value = pair[1];
        } else {
            // TODO: incorrect string
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
