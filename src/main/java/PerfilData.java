import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            this.email = (String) session.getAttribute("userEmail");

            if (this.email != null) {
                cargarPerfilDesdeAPI(this.email);
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Correo no encontrado en la sesión", ""));
            }
        }
    }

    private void cargarPerfilDesdeAPI(String email) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/ApiCRM/api/usuarios/consultarPorCorreo/" + email;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servidor: " + response.body()); // Verifica el JSON recibido

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());

                this.id = rootNode.path("id").asText(); // ✅ Se obtiene el ID correctamente
                this.nombre = rootNode.path("nombre").asText();
                this.apellidoPaterno = rootNode.path("apellidoPaterno").asText();
                this.apellidoMaterno = rootNode.path("apellidoMaterno").asText();
                this.telefono = rootNode.path("telefono").asText();
                this.email = rootNode.path("email").asText();
                this.direccion = rootNode.path("direccion").asText();
                this.ciudad = rootNode.path("ciudad").asText();
                this.codigoPostal = rootNode.path("codigoPostal").asText();
                this.numCuenta = rootNode.path("numCuenta").asText();

                System.out.println("✅ ID extraído correctamente: " + this.id);

                // Guarda el ID en la sesión
                FacesContext context = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                session.setAttribute("userId", this.id);

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

        if (this.id == null || this.id.isEmpty()) {
            System.out.println("⚠️ ID no disponible. No se puede actualizar el perfil.");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El ID no está disponible", ""));
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(this);
            System.out.println("📤 JSON generado para actualizar: " + json);

            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/ApiCRM/api/usuarios/modificar/" + this.email;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servidor al actualizar: " + response.body());

            if (response.statusCode() == 200) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Perfil actualizado correctamente", ""));
                context.getExternalContext().redirect("Product.xhtml");
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al actualizar el perfil", ""));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "❌ Error al actualizar el perfil", e);
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
