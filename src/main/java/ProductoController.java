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

    private List<Producto> productos;
    private String productoId; // Cambiado a String para consistencia
    private Producto productoSeleccionado;
    private List<Producto> listaDeseos = new ArrayList<>();
    private String selectedProductId;
    private Producto selectedProduct;

    @PostConstruct
    public void init() {
        consultarProductos();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public String getProductoId() { // Cambiado a String
        return productoId;
    }

    public void setProductoId(String productoId) { // Cambiado a String
        this.productoId = productoId;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public List<Producto> getListaDeseos() { // Añadido getter
        return listaDeseos;
    }

    public void eliminarDeListaDeseos(Producto producto) {
        if (producto != null && listaDeseos.contains(producto)) {
            listaDeseos.remove(producto);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Producto eliminado de la lista de deseos", null));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "El producto no está en la lista de deseos", null));
        }
    }

    public void consultarProductos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/APICRM2/api/productos")) // Resuelto conflicto de merge
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());

            if (response.statusCode() == 200) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    this.productos = Arrays.asList(objectMapper.readValue(response.body(), Producto[].class));
                } catch (IOException e) {
                    System.err.println("Error al procesar la respuesta JSON: " + e.getMessage());
                }
            }

        } catch (IOException | InterruptedException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la conexión con la API", null));
        }
    }

    public String verDetalle(Producto producto) {
        this.selectedProduct = producto;
        return "detalleProducto?faces-redirect=true";
    }
    
    public void loadSelectedProduct() {
        if (selectedProductId != null && productos != null) {
            selectedProduct = productos.stream()
                .filter(p -> p.getId().toString().equals(selectedProductId)) // Convertir ID a String para comparar
                .findFirst()
                .orElse(null);
        }
        System.out.println(selectedProductId+"####3");
    }

    // Getters y Setters
    public String getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(String selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    public Producto getSelectedProduct() {
        return selectedProduct;
    }
}
