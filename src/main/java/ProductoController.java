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
    private Integer productoId;
    private Producto productoSeleccionado;
    private List<Producto> listaDeseos = new ArrayList<>();


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

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
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
                    .uri(URI.create("https://4ccb-2806-2f0-9020-9bac-d4c-95c5-fa1-4e06.ngrok-free.app/APICRM2/api/productos"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                productos = Arrays.asList(objectMapper.readValue(response.body(), Producto[].class));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al obtener los productos", null));
            }
        } catch (IOException | InterruptedException e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la conexión con la API", null));
        }
    }

    public String verDetalle(Producto producto) {
        this.productoSeleccionado = producto;
        return "detalleProducto?faces-redirect=true";
    }
}