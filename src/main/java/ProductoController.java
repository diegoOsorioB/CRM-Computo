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
import java.util.Arrays;
import java.text.Normalizer;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private boolean apiConectada = true;
    private boolean noResultados = false;

    @PostConstruct
    public void init() {
        consultarProductos();
    }

    // Método para normalizar las cadenas y eliminar acentos
    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); // Eliminar marcas diacríticas (acentos)
    }

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
            this.productosFiltrados = new ArrayList<>(productos);

            System.out.println("Se han guardado " + this.productos.size() + " productos en total.");

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error con el servidor intente mas tarde", null));
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

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                Producto[] productosArray = objectMapper.readValue(response.body(), Producto[].class);
                productos = Arrays.asList(productosArray);
            } else {
                // Marca un estado de error, sin mostrar detalles del JSON
                this.apiConectada = false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.apiConectada = false;
        }
        return productos;
    }

    public boolean isApiConectada() {
        return apiConectada;
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
        System.out.println(selectedProductId + "####3");
    }

    // Métodos de búsqueda con normalización
    public List<Producto> buscarProducto(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return productos; // Si no hay criterio, devuelve la lista completa.
        }

        String criterioLower = normalizeString(criterio).toLowerCase(); // Normaliza y convierte a minúsculas

        return productos.stream()
                .filter(p -> normalizeString(p.getNombre()).toLowerCase().contains(criterioLower))
                .collect(Collectors.toList());
    }

    public void filtrarProductos() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
            noResultados = false;
        } else {
            String criterioLower = normalizeString(criterioBusqueda).toLowerCase(); // Normaliza y convierte a minúsculas
            productosFiltrados = productos.stream()
                    .filter(p -> normalizeString(p.getNombre()).toLowerCase().contains(criterioLower))
                    .collect(Collectors.toList());
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
