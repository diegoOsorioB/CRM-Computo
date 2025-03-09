
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
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
    private String imageUrl;
    private Part imageFile;

    public PerfilData() {
        cargarDatosDesdeJSON();
    }

    private void cargarDatosDesdeJSON() {
        // Simulación de la recepción del JSON desde el ERP
        String jsonData = "{ \"firstName\": \"Juan\", \"lastName\": \"Perez\", \"email\": \"juan.perez@example.com\", \"username\": \"juanp\", \"street\": \"Av. Siempre Viva\", \"city\": \"Springfield\", \"country\": \"USA\", \"postalCode\": \"12345\", \"bankInfo\": \"Cuenta 123456\" }";

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
            this.imageUrl = rootNode.path("imageUrl").asText();  // Cargar la URL de la imagen desde el JSON
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al procesar el JSON del ERP", e);
        }
    }

    // Actualizar el perfil (guardar cambios)
    public void updateProfile() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Perfil actualizado correctamente", ""));
    }

    // Subir la nueva imagen de perfil
    public void uploadImage() {
        if (imageFile != null) {
            // Simula guardar la imagen en el servidor y actualizar la URL
            this.imageUrl = "/uploads/" + imageFile.getSubmittedFileName();  // Aquí puedes manejar el archivo real y guardarlo en el servidor
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Imagen actualizada correctamente"));
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
