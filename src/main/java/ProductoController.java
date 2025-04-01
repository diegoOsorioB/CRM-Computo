import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Named
@RequestScoped
public class ProductoController {
    private List<Producto> productos;
    private List<Producto> productosFiltrados;
        private String selectedProductId;
    private Producto selectedProduct;
     private String message;
     private String criterioBusqueda = "";
     
     @PostConstruct
public void init() {
    consultarProductos();
}
    
    APISController api = new APISController();

    public ProductoController() {
        this.productos = new ArrayList<>();
        this.productosFiltrados = new ArrayList<>();
    }

    public void consultarProductos() {
        try {
            List<Producto> productosAPI1 = obtenerProductosDesdeAPI(api.getAPI_URL_1());
            List<Producto> productosAPI2 = obtenerProductosDesdeAPI(api.getAPI_URL_2());

            // Fusionar ambas listas en una sola
            this.productos = new ArrayList<>();
            this.productos.addAll(productosAPI1);
            this.productos.addAll(productosAPI2);

            // Clonar la lista para productosFiltrados
            this.productosFiltrados = new ArrayList<>(productos);

            System.out.println("Se han guardado " + this.productos.size() + " productos en total.");

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la conexión con las APIs", null));
        }
    }

    private List<Producto> obtenerProductosDesdeAPI(String apiUrl) {
        List<Producto> productos = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta JSON de " + apiUrl + ": " + response.body());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                Producto[] productosArray = objectMapper.readValue(response.body(), Producto[].class);
                productos = Arrays.asList(productosArray);
                
                System.out.println("Se han recibido " + productos.size() + " productos desde " + apiUrl);
            } else {
                System.out.println("Error: Código de respuesta HTTP " + response.statusCode() + " en " + apiUrl);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return productos;
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

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Producto> getProductosFiltrados() {
        return productosFiltrados;
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
