
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@SessionScoped
public class PerfilData implements Serializable {

    APISController api = new APISController();

    private static final Logger LOGGER = Logger.getLogger(PerfilData.class.getName());

    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String email;
    private String password = "";
    private String direccion = "";
    private String ciudad;
    private String codigoPostal;
    private String numCuenta;
    private String passwordConfirm = "";
    private String imagenUrl = "";

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        this.email = context.getExternalContext().getRequestParameterMap().get("email");

        if (this.email != null) {
            cargarPerfilDesdeAPI(this.email);
        }
    }

    private void cargarPerfilDesdeAPI(String correo) {
        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/usuarios?correo=" + correo;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            System.out.println("El token Admin " + token);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servidor: " + response.body()); // Verifica el JSON recibido

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                // Deserializa el JSON como un array de objetos
                JsonNode arrayNode = mapper.readTree(response.body());

                // Acceder al primer objeto del array
                JsonNode rootNode = arrayNode.get(0); // Obtener el primer elemento del array

                // Extraer los datos y asignarlos a los atributos de la clase PerfilData
                this.id = rootNode.path("_id").asText();
                this.nombre = rootNode.path("nombre").asText();
                this.apellidoPaterno = rootNode.path("apellidoPaterno").asText();
                this.apellidoMaterno = rootNode.path("apellidoMaterno").asText();
                this.telefono = rootNode.path("telefono").asText();
                this.email = rootNode.path("correo").asText();
                this.direccion = rootNode.path("direccion").asText();
                this.ciudad = rootNode.path("ciudad").asText();
                this.codigoPostal = rootNode.path("codigoPostal").asText();
                this.numCuenta = rootNode.path("numCuenta").asText();

                System.out.println("✅ ID extraído correctamente: " + this.id);

                // Guardar el ID en la sesión
                HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                session.setAttribute("userId", this.id);

                // Establecer una imagen predeterminada si no existe
                if (this.imagenUrl == null || this.imagenUrl.isEmpty()) {
                    this.imagenUrl = "/uploads/default.jpg";
                }

                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Perfil cargado correctamente", ""));
            } else {
                LOGGER.log(Level.SEVERE, "❌ Error al obtener el perfil: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "❌ Error al procesar la solicitud de perfil", e);
        }
    }

    public void updateProfile() {
        System.out.println("🔄 Entró al método updateProfile");

        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");

        if (this.id == null || this.id.isEmpty()) {
            System.out.println("⚠️ ID no disponible. No se puede actualizar el perfil.");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El ID no está disponible", ""));
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Crear un nodo JSON manualmente para poder eliminar el campo de contraseña si es vacío
            JsonNode jsonNode = mapper.valueToTree(this);
            ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode).remove("passwordConfirm"); // Remover passwordConfirm

            // Si la contraseña está vacía, eliminarla del JSON para que no se envíe
            if (this.password == null || this.password.trim().isEmpty()) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode).remove("password");
            }

            String json = mapper.writeValueAsString(jsonNode);
            System.out.println("📤 JSON generado para actualizar: " + json);

            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/usuarios/" + this.id;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servidor al actualizar: " + response.body());

            if (response.statusCode() == 200) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Perfil actualizado correctamente", ""));
                context.getExternalContext().redirect("Product.xhtml");
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al actualizar el perfil", ""));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "❌ Error al actualizar el perfil", e);
        }
    }

    public void updatePassword() {
    System.out.println("🔒 Entró al método updatePassword");
    
    FacesContext context = FacesContext.getCurrentInstance();
    String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");

    if (this.email == null || this.email.isEmpty()) {
        System.out.println("⚠️ Email no disponible. No se puede actualizar la contraseña.");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El correo no está disponible", ""));
        return;
    }

    cargarPerfilDesdeAPI(this.email); // Obtener el usuario correcto

    if (this.id == null || this.id.isEmpty()) {
        System.out.println("⚠️ ID no disponible. No se puede actualizar la contraseña.");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El ID no está disponible", ""));
        return;
    }

    if (this.password == null || this.password.trim().isEmpty() || this.passwordConfirm == null || this.passwordConfirm.trim().isEmpty()) {
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "La contraseña no puede estar vacía", ""));
        return;
    }

    if (!this.password.equals(this.passwordConfirm)) {
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Las contraseñas no coinciden", ""));
        return;
    }

    try {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("password", this.password);

        String json = mapper.writeValueAsString(jsonNode);
        System.out.println("📤 JSON generado para actualizar contraseña: " + json);

        HttpClient client = HttpClient.newHttpClient();
        String url = api.getURLBD() + "/usuarios/" + this.id; // Asegurar que se usa el ID correcto

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📥 Respuesta del servidor al actualizar contraseña: " + response.body());

        if (response.statusCode() == 200) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Contraseña actualizada correctamente", ""));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al actualizar la contraseña", ""));
        }
    } catch (IOException | InterruptedException e) {
        LOGGER.log(Level.SEVERE, "❌ Error al actualizar la contraseña", e);
    }
}


    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getNumCuenta() {
        return numCuenta;
    }

    public void setNumCuenta(String numCuenta) {
        this.numCuenta = numCuenta;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
