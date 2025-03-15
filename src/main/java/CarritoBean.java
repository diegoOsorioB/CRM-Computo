
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
        // Verificar si el producto ya está en el carrito
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                // Si ya está en el carrito, solo incrementa la cantidad
                item.setCantidad(item.getCantidad() + 1);
                return;
            }
        }
        // Si no está en el carrito, agregarlo con cantidad 1
        items.add(new ItemCarrito(producto, 1));
        System.out.println("Producto agregado: " + producto.getNombre());
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
