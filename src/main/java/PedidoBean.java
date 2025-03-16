import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class PedidoBean {

    @Inject
    private PedidoService pedidoService;

    public List<Pedido> getPedidos() {
        List<Pedido> pedidos = pedidoService.obtenerPedidos();
        
        if (pedidos.isEmpty()) {
            System.out.println("❌ No hay pedidos almacenados.");
        } else {
            System.out.println("📋 Mostrando pedidos: " + pedidos.size());
        }

        return pedidos;
    }
}
