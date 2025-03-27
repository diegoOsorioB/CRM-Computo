import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("productoBean")
@ViewScoped
public class ProductoBean implements Serializable {

    private List<Producto> productos;
    private List<Producto> productosFiltrados;
    private Producto producto;
    private String criterioBusqueda;

    public ProductoBean() {
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Producto> getProductosFiltrados() {
        return productosFiltrados;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getCriterioBusqueda() {
        return criterioBusqueda;
    }

    public void setCriterioBusqueda(String criterioBusqueda) {
        this.criterioBusqueda = criterioBusqueda;
    }

    public void buscarProducto() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
        } else {
            String criterio = criterioBusqueda.toLowerCase();
            productosFiltrados = productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(criterio))
                .collect(Collectors.toList());
        }
    }

   public void cargarProducto() {
    FacesContext fc = FacesContext.getCurrentInstance();
    Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
    String idStr = params.get("id");

    // Verifica si el parámetro 'id' está siendo recibido
    System.out.println("Parámetro 'id' recibido: " + idStr);

    if (idStr != null) {
        try {
            int id = Integer.parseInt(idStr);
            System.out.println("ID convertido: " + id);  // Verifica si se convierte correctamente

            // Filtrar el producto con el ID recibido
            producto = productos.stream()
                                .filter(p -> p.getId() == id)
                                .findFirst()
                                .orElse(null);

            if (producto == null) {
                System.out.println("No se encontró un producto con ID: " + id);  // Verifica si el producto es null
            }

        } catch (NumberFormatException e) {
            producto = null;
            System.out.println("Error al convertir el id: " + idStr);
        }
    }
}

}
