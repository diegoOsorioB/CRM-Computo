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
import org.json.JSONObject;

@Named
@RequestScoped
public class ProductoController {

    private List<Producto> productos;
    private Producto productoSeleccionado;
    private List<Producto> listaDeseos = new ArrayList<>();
    private String selectedProductId;
    private Producto selectedProduct;
     private String message;
     private String criterioBusqueda = "";
     private List<Producto> productosFiltrados = new ArrayList<>();
     APISController api = new APISController();

    @PostConstruct
    public void init() {
        consultarProductos();
    }

    public List<Producto> getProductos() {
        return productosFiltrados.isEmpty() ? productos : productosFiltrados;
    }


    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
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
                    .uri(URI.create(api.getURLERPpro()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());

            if (response.statusCode() == 200) {
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        this.productos = Arrays.asList(objectMapper.readValue(response.body(), Producto[].class)); // Asignar a la variable de instancia
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
        if (selectedProductId != null) {
            selectedProduct = productos.stream()
                .filter(p -> p.getId().equals(selectedProductId))
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
    
    
    public List<Producto> buscarProducto(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return productos; // Si no hay criterio, devuelve la lista completa.
        }

        String criterioLower = criterio.toLowerCase();

        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(criterioLower))
                .toList();
    }

    private boolean noResultados; // Nueva variable para controlar la visibilidad del mensaje

    public void filtrarProductos() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
            noResultados = false;
        } else {
            String criterioLower = criterioBusqueda.toLowerCase();
            productosFiltrados = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(criterioLower))
                    .toList();
            noResultados = productosFiltrados.isEmpty(); // True si no hay productos
        }
    }

    public boolean isNoResultados() {
        return noResultados;
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