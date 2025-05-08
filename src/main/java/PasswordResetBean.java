import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.primefaces.PrimeFaces;

@Named
@RequestScoped
public class PasswordResetBean implements Serializable {

    private String email;
    private String newPassword;
    private String confirmPassword;
    private String mensaje;
    private String id;
    private String token;
    String mensajePD;

    private final APISController api = new APISController();

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        email = context.getExternalContext().getRequestParameterMap().get("email");
    }

    
    public void consultarP() {
        try {
            
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"correo\":\"maximiliano@max.com\",\"password\":\"12345678\" }");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api.getURLLOGIN()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: Admin  " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Conexión exitosa");

                // Convertir la respuesta en JSON
                JSONObject jsonResponse = new JSONObject(response.body());

                 token = jsonResponse.getString("token"); // Asegúrate de que el backend envíe el token
                System.out.println(response.body() + " Max");

                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                // Guardar datos en la sesión
                session.setAttribute("authTokenA", token);

            } else {
                System.out.println("Error en el login: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales inválidas", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error de conexión con la API: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Servicio no disponible. Detalles: " + e.getClass().getSimpleName(),
                            null));
        }

    }
    
    private void cargarPerfilDesdeAPI() {

        try {
            consultarP();
            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/clientes?correo=" + email;
            // String url = "https://afef-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/service/usuarios?correo=" + correo;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            System.out.println("El token Admin " + token);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("?:p' Respuesta del servidor: " + response.body()); // Verifica el JSON recibido

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                // Deserializa el JSON como un array de objetos
                JsonNode arrayNode = mapper.readTree(response.body());

                // Acceder al primer objeto del array
                if (arrayNode.isArray() && arrayNode.size() > 0) {
                    JsonNode rootNode = arrayNode.get(0);
                    if (rootNode != null) {
                        this.id = rootNode.path("_id").asText();
                        

                        System.out.println("✅ ID extraído correctamente: " + this.id);
                    } else {
                        mensajePD = "Informacion de usuario no encontrada";
                    }
                } else {
                    System.out.println("⚠️ La API devolvió un array vacío.");
                }

                System.out.println("✅ ID extraído correctamente: " + this.id);

                // Guardar el ID en la sesión
              

                // Establecer una imagen predeterminada si no existe
              

            } else {
                mensajePD = "Error: " + response.statusCode();

                PrimeFaces.current().executeScript("PF('erpDialog').show();");
            }
        } catch (IOException | InterruptedException e) {
            mensajePD = "Error: " + e;

            PrimeFaces.current().executeScript("PF('erpDialog').show();");
        }
    }
    
    public void updateProfile() {
        System.out.println("🔄 Entró al método updateProfile");
        cargarPerfilDesdeAPI();
        

        if (this.id == null || this.id.isEmpty()) {
            mensajePD = "Error: Usuario no encontrado";
            PrimeFaces.current().executeScript("PF('erpDialog').show();");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonBody = mapper.createObjectNode();

            // Solo se actualiza la contraseña si no está vacía
            if (this.newPassword != null && !this.newPassword.trim().isEmpty()) {
                
                jsonBody.put("password", this.newPassword);
                

            }

            String json = mapper.writeValueAsString(jsonBody);
            System.out.println("📤 JSON generado para actualizar (manual): " + json);

            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/clientes/" + this.id;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servidor al actualizar: " + response.body());

            if (response.statusCode() == 200) {
                mensajePD = "Datos actualizados correctamente";
                PrimeFaces.current().executeScript("PF('erpDialog').show();");
            } else {
                mensajePD = "Error: " + response.statusCode();
                PrimeFaces.current().executeScript("PF('erpDialog').show();");
            }
        } catch (IOException | InterruptedException e) {
            mensajePD = "Error: " + e;
            PrimeFaces.current().executeScript("PF('erpDialog').show();");
        }
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

            String url = api.getURLBD() + "/clientes?correo=" + email;

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
