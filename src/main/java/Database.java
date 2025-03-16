import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Map<String, String> tokenStore = new HashMap<>();
    private static Map<String, String> users = new HashMap<>();

    public static void savePasswordResetToken(String email, String token) {
        tokenStore.put(token, email);
    }

    public static String getEmailByToken(String token) {
        return tokenStore.get(token);
    }

    public static void updateUserPassword(String email, String newPassword) {
        users.put(email, newPassword);
    }
}
