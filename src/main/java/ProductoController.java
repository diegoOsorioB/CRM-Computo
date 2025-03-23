import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

@Named
@RequestScoped
public class ProductoController {

    private List<Producto> productos;

    @PostConstruct
    public void init() {
        consultarProductos();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public void consultarProductos() {
        System.out.println("eENTRA PERO*****************************");
        try {
            System.out.println("Entro a los productos");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/APICRM2/api/productos"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                productos = Arrays.asList(objectMapper.readValue(response.body(), Producto[].class));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al obtener los productos", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la conexión con la API", null));
        }
    }
}
