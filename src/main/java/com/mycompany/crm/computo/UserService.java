/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.crm.computo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static Map<String, String> users = new HashMap<>();

    static {
        // Simulación del JSON obtenido desde el ERP
        String jsonFromERP = """
        {
            "users": [
                { "username": "admin", "password": "1234", "email": "admin@example.com" },
                { "username": "user1", "password": "pass1", "email": "user1@example.com" }
            ]
        }
        """;

        JSONObject jsonObj = new JSONObject(jsonFromERP);
        JSONArray jsonArray = jsonObj.getJSONArray("users");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject userObj = jsonArray.getJSONObject(i);
            users.put(userObj.getString("username"), userObj.getString("password"));
        }
    }

    public static boolean validateUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }
}
