import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("carritoBean")
@SessionScoped
public class CarritoBean implements Serializable {

    private List<ItemCarrito> items = new ArrayList<>();

    @Inject
    private ProductoBean productoBean;
    
    @Inject
    private favoritosBean favoritosBean;

    @Inject
    private ListaDeseosBean listaDeseosBean;  // Nueva inyección

    public String agregarProductoActual() {
        if (productoBean.getProducto() != null) {
            agregarProducto(productoBean.getProducto());
            return "Carrito.xhtml";
        } else {
            System.out.println("Producto no encontrado.");
        }
        return "Producto no encontrado.";
    }

    public void agregarProducto(Producto producto) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                item.setCantidad(item.getCantidad() + 1);
                return;
            }
        }
        items.add(new ItemCarrito(producto, 1));
        System.out.println("Producto agregado: " + producto.getNombre());
    }

    public void agregarAFavoritos(Producto producto) {
        if (producto != null) {
            favoritosBean.agregarAFavoritos(producto);
            System.out.println("Producto agregado a favoritos: " + producto.getNombre());
        }
    }

    public void agregarAListaDeseos(Producto producto) {
        if (producto != null) {
            listaDeseosBean.agregarAListaDeseos(producto);
            System.out.println("Producto agregado a la lista de deseos: " + producto.getNombre());
        }
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void incrementarCantidad(Producto producto) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                item.setCantidad(item.getCantidad() + 1);
                break;
            }
        }
    }

    public void decrementarCantidad(Producto producto) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == producto.getId() && item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1);
                break;
            }
        }
    }

    public void eliminarProducto(Producto producto) {
        items.removeIf(item -> item.getProducto().getId() == producto.getId());
    }

    public void vaciarCarrito() {
        items.clear();
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();
    }
}
