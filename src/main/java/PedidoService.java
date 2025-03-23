import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class PedidoService {
    private List<Pedido> pedidos = new ArrayList<>();
    private User user=new User();

    public void agregarPedido(List<ItemCarrito> items, double total,String direccion) {
        if (items == null || items.isEmpty()) {
            System.out.println("❌ ERROR: No hay productos en el pedido.");
            return;
        }

        Pedido nuevoPedido = new Pedido(items, total, "En proceso",direccion,user.getEmail());
        pedidos.add(nuevoPedido);

        System.out.println("✅ Pedido agregado correctamente: ");
        System.out.println("   Total: $" + total);
        System.out.println("   Productos: " + items.size());
    }

    public List<Pedido> obtenerPedidos() {
        return pedidos;
    }
}
