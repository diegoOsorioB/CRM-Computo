import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("favoritosBean")
@SessionScoped
public class favoritosBean implements Serializable {

    private List<Producto> favoritos = new ArrayList<>();

    @Inject
    private ProductoBean productoBean;

    public void agregarProductoAFavoritos() {
        Producto producto = productoBean.getProducto();
        if (producto != null) {
            agregarAFavoritos(producto);
        }
    }

    public void agregarAFavoritos(Producto producto) {
        if (producto != null && !favoritos.contains(producto)) {
            favoritos.add(producto);
        }
    }

    public void eliminarDeFavoritos(Producto producto) {
        favoritos.remove(producto);
    }

    public void vaciarFavoritos() {
        favoritos.clear();
    }

    public List<Producto> getFavoritos() {
        return favoritos;
    }
}