import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class PerfilData implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PerfilData.class.getName());

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String street;
    private String city;
    private String country;
    private String postalCode;
    private String bankInfo;
    private String imageUrl;//="https://www.google.com/imgres?q=imagen%20png&imgurl=https%3A%2F%2Fraulperez.tieneblog.net%2Fwp-content%2Fuploads%2F2015%2F09%2Ftux.jpg&imgrefurl=https%3A%2F%2Fraulperez.tieneblog.net%2Fconvertir-imagen-jpg-a-png-con-gimp%2F&docid=5AKTfWxOhe2f3M&tbnid=c-606BS9DAzetM&vet=12ahUKEwiYsKTCjvyLAxVHHUQIHQT0BToQM3oECHQQAA..i&w=256&h=256&hcb=2&ved=2ahUKEwiYsKTCjvyLAxVHHUQIHQT0BToQM3oECHQQAA";
    private Part imageFile;

    private static final String UPLOAD_DIR = "/opt/uploads"; // Ruta donde se guardarán las imágenes

    @PostConstruct
    public void init() {
        cargarDatosDesdeJSON();
    }

    private void cargarDatosDesdeJSON() {
        // Simulación de la recepción del JSON desde el ERP
        String jsonData = "{ \"firstName\": \"Juan\", \"lastName\": \"Perez\", \"email\": \"juan.perez@example.com\", \"username\": \"juanp\", \"street\": \"Av. Siempre Viva\", \"city\": \"Springfield\", \"country\": \"USA\", \"postalCode\": \"12345\", \"bankInfo\": \"Cuenta 123456\", \"imageUrl\": \"default.jpg\" }";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);

            this.firstName = rootNode.path("firstName").asText();
            this.lastName = rootNode.path("lastName").asText();
            this.email = rootNode.path("email").asText();
            this.username = rootNode.path("username").asText();
            this.street = rootNode.path("street").asText();
            this.city = rootNode.path("city").asText();
            this.country = rootNode.path("country").asText();
            this.postalCode = rootNode.path("postalCode").asText();
            this.bankInfo = rootNode.path("bankInfo").asText();
            this.imageUrl = rootNode.path("imageUrl").asText();

            // Si la imagen no está definida, asignar una por defecto
            if (this.imageUrl == null || this.imageUrl.isEmpty()) {
                this.imageUrl = "/home/diego/Descargas/linu.jpg";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al procesar el JSON del ERP", e);
        }
    }

    // Método para actualizar el perfil
    public void updateProfile() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Perfil actualizado correctamente", ""));
    }

    // Método para subir la imagen de perfil
    public void uploadImage() {
        if (imageFile != null && imageFile.getSize() > 0) {
            try {
                String fileName = Paths.get(imageFile.getSubmittedFileName()).getFileName().toString();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                // Crear la carpeta si no existe
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Guardar el archivo en el servidor
                Path filePath = uploadPath.resolve(fileName);
                try (InputStream input = imageFile.getInputStream()) {
                    Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
                }

                // Actualizar la URL de la imagen para mostrarla en la interfaz
                this.imageUrl = "/uploads/" + fileName;

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Imagen actualizada correctamente"));

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error al subir la imagen", e);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al subir la imagen", ""));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe seleccionar una imagen", ""));
        }
    }

    // Getters y Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Part getImageFile() {
        return imageFile;
    }

    public void setImageFile(Part imageFile) {
        this.imageFile = imageFile;
    }
}
