package org.cytomine.keycloak;

public class CustomRolesMapper {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new CustomRolesMapper().getGreeting());
    }
}
