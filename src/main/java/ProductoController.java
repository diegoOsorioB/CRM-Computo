import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@RequestScoped
public class ProductoController {

    private List<Producto> productos = new ArrayList<>();
    private List<Producto> productosFiltrados = new ArrayList<>();
    private String criterioBusqueda = "";

    @PostConstruct
    public void init() {
        consultarProductos();
    }

    public List<Producto> getProductos() {
        return productosFiltrados.isEmpty() ? productos : productosFiltrados;
    }

    public void consultarProductos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/products"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                this.productos = Arrays.asList(objectMapper.readValue(response.body(), Producto[].class));
                this.productosFiltrados = new ArrayList<>(productos);
            }

        } catch (IOException | InterruptedException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la conexión con la API", null));
        }
    }

    public void filtrarProductos() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
        } else {
            String criterioLower = criterioBusqueda.toLowerCase();
            productosFiltrados = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(criterioLower))
                    .toList();
        }

        if (productosFiltrados.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Producto no encontrado", null));
        }
    }

    // Getters y Setters
    public String getCriterioBusqueda() {
        return criterioBusqueda;
    }

    public void setCriterioBusqueda(String criterioBusqueda) {
        this.criterioBusqueda = criterioBusqueda;
        filtrarProductos();
    }
}
