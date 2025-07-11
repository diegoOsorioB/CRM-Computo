
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
import org.primefaces.PrimeFaces;

@Named
@SessionScoped
public class PerfilData implements Serializable {

    APISController api = new APISController();
    private String mensajePD;

    public String getMensajePD() {
        return mensajePD;
    }

    public void setMensajePD(String mensajePD) {
        this.mensajePD = mensajePD;
    }

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
        System.out.println("🚀 Método init() ejecutado");

        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            this.email = (String) session.getAttribute("userEmail");
        }

        System.out.println("📧 Email obtenido de la sesión: " + this.email);

        if (this.email != null) {
            cargarPerfilDesdeAPI(this.email);
        } else {
            mensajePD = "Servicio no disponible";

            PrimeFaces.current().executeScript("PF('erpDialog').show();");
        }
    }

    private void cargarPerfilDesdeAPI(String correo) {
        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/clientes?correo=" + correo;
            // String url = "https://afef-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/service/usuarios?correo=" + correo;

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
                if (arrayNode.isArray() && arrayNode.size() > 0) {
                    JsonNode rootNode = arrayNode.get(0);
                    if (rootNode != null) {
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
                    } else {
                        mensajePD = "Informacion de usuario no encontrada";
                    }
                } else {
                    System.out.println("⚠️ La API devolvió un array vacío.");
                }

                System.out.println("✅ ID extraído correctamente: " + this.id);

                // Guardar el ID en la sesión
                HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                session.setAttribute("userId", this.id);

                // Establecer una imagen predeterminada si no existe
                if (this.imagenUrl == null || this.imagenUrl.isEmpty()) {
                    this.imagenUrl = "/uploads/default.jpg";
                }

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
        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");

        if (this.id == null || this.id.isEmpty()) {
            mensajePD = "Error: Usuario no encontrado";
            PrimeFaces.current().executeScript("PF('erpDialog').show();");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonBody = mapper.createObjectNode();

            // Campos obligatorios o comunes
            jsonBody.put("nombre", this.nombre);
            jsonBody.put("apellidoPaterno", this.apellidoPaterno);
            jsonBody.put("apellidoMaterno", this.apellidoMaterno);
            jsonBody.put("telefono", this.telefono);
            jsonBody.put("correo", this.email);
            jsonBody.put("direccion", this.direccion);
            jsonBody.put("ciudad", this.ciudad);
            jsonBody.put("codigoPostal", this.codigoPostal);
            jsonBody.put("numCuenta", this.numCuenta);

            // Solo se actualiza la contraseña si no está vacía
            if (this.password != null && !this.password.trim().isEmpty()) {
                
                jsonBody.put("password", this.password);
                

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
                context.getExternalContext().redirect("Product.xhtml");
            } else {
                mensajePD = "Error: " + response.statusCode();
                PrimeFaces.current().executeScript("PF('erpDialog').show();");
            }
        } catch (IOException | InterruptedException e) {
            mensajePD = "Error: " + e;
            PrimeFaces.current().executeScript("PF('erpDialog').show();");
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
