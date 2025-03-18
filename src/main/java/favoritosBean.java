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

    public String agregarProductoAFavoritos() {
        if (productoBean.getProducto() != null) {
            agregarAFavoritos(productoBean.getProducto());
            return "Favoritos.xhtml";
        } else {
            System.out.println("Producto no encontrado.");
        }
        return "Producto no encontrado.";
    }

    public void agregarAFavoritos(Producto producto) {
        if (!favoritos.contains(producto)) {
            favoritos.add(producto);
            System.out.println("Producto agregado a favoritos: " + producto.getNombre());
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