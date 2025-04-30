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
    if (producto != null) {
        for (ItemCarrito item : items) {
            if (item.getProducto() != null && item.getProducto().getId() != null && producto.getId() != null && item.getProducto().getId().equals(producto.getId())) {
 
                if (item.getStockTemporal() > 0) {
                    item.setCantidad(item.getCantidad() + 1);
                    item.setStockTemporal(item.getStockTemporal() - 1); // Reducir stock temporal
                } else {
                    System.out.println("⚠️ Stock insuficiente para " + producto.getNombre());
                }
                return;
            }
        }

        // Nuevo producto en el carrito
        if (producto.getStock() > 0) {
            ItemCarrito nuevoItem = new ItemCarrito(producto, 1);
            nuevoItem.setStockTemporal(producto.getStock() - 1);
            items.add(nuevoItem);
            System.out.println("🛒 Producto agregado: " + producto.getNombre());
        } else {
            System.out.println("⚠️ Stock insuficiente para " + producto.getNombre());
        }
    }
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
            if (item.getProducto().getId().equals(producto.getId())) {
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
    items.removeIf(item -> {
        if (item.getProducto().getId().equals(producto.getId())) {
            producto.setStock(producto.getStock() + item.getCantidad()); // Restaurar stock
            return true;
        }
        return false;
    });
}

public void vaciarCarrito() {
    for (ItemCarrito item : items) {
        item.getProducto().setStock(item.getProducto().getStock() + item.getCantidad()); // Restaurar stock
    }
    items.clear();
}


    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();
    }
}
