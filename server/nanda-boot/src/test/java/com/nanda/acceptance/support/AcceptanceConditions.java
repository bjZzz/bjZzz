package com.nanda.acceptance.support;

import java.sql.Connection;
import java.sql.DriverManager;

public final class AcceptanceConditions {

    private AcceptanceConditions() {
    }

    public static boolean mysqlAvailable() {
        String url = System.getenv("ACCEPTANCE_DB_URL");
        if (url == null || url.isEmpty()) {
            url = "jdbc:mysql://localhost:3306/nanda?useUnicode=true&characterEncoding=utf8"
                    + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
        }
        String user = System.getenv("DB_USER");
        if (user == null || user.isEmpty()) {
            user = "nanda";
        }
        String pass = System.getenv("DB_PASS");
        if (pass == null || pass.isEmpty()) {
            pass = "nanda123";
        }
        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            if (!connection.isValid(2)) {
                return false;
            }
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT 1").close();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
