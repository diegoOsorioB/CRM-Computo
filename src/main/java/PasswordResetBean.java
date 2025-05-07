import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Named
@RequestScoped
public class PasswordResetBean implements Serializable {

    private String email;
    private String newPassword;
    private String confirmPassword;
    private String mensaje;

    private final APISController api = new APISController();

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        email = context.getExternalContext().getRequestParameterMap().get("email");
    }

    public void resetPassword() {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            mensaje = "Las contraseñas no coinciden.";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();

            body.put("password", newPassword);

            String json = mapper.writeValueAsString(body);

            String url = api.getURLBD() + "/clientes/reset-password?correo=" + email;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                mensaje = "Contraseña actualizada correctamente.";
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
            } else {
                mensaje = "Error al actualizar la contraseña: " + response.statusCode();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
            }

        } catch (IOException | InterruptedException e) {
            mensaje = "Error de conexión: " + e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
        }
    }

    // Getters y Setters
    public String getEmail() {
        return email;
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

    public String getMensaje() {
        return mensaje;
    }
}
