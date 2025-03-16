import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@RequestScoped
public class PasswordResetBean implements Serializable {
    private String token;
    private String newPassword;
    private String confirmPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void resetPassword() {
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Las contraseñas no coinciden.");
            return;
        }

        // Obtener correo asociado al token y actualizar la contraseña
        String email = Database.getEmailByToken(token);
        if (email != null) {
            Database.updateUserPassword(email, newPassword);
            System.out.println("Contraseña restablecida correctamente.");
        } else {
            System.out.println("Token inválido o expirado.");
        }
    }
}
