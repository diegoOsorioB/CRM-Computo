import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class PedidoBean implements Serializable {
    private List<Compra> listaPedidos;

    @PostConstruct
    public void init() {
        cargarPedidos();
    }
    
    @Inject
    private EmailService emailService;
        
    private Compra pedidoSeleccionado;

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




    private void cargarPedidos() {
        listaPedidos = new ArrayList<>();
        listaPedidos.add(new Compra(101, "2025-03-01", 250.00, "Pendiente", new User("1", "Juan", "Perez","osoriodiego151@gmail.com")));
        listaPedidos.add(new Compra(102, "2025-03-05", 100.00, "En Proceso", new User("2", "Maria", "Lopez", "lopez@gmail.com")));
        listaPedidos.add(new Compra(103, "2025-03-07", 75.50, "Enviado", new User("3", "Carlos", "Hernandez", "hernanddez@gmail.com")));
        listaPedidos.add(new Compra(104, "2025-03-10", 300.75, "Entregado", new User("4", "Ana", "Ramirez", "leopablo26@gmail.com")));
    }

    public void actualizarEstado(Compra pedido) {
        if (pedido.getEstado().equals(pedido.getEstadoInicial())){
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Estado actualizado:"+"El pedido #" + pedido.getIdPedido() + " ahora está en estado: " + pedido.getEstado(),""));
            pedidoSeleccionado=pedido;
            actualizarEstadoPedido();
        }
    }
    
    public void actualizarEstadoPedido() {
        if (pedidoSeleccionado != null) {
            try {
                // Simulación de actualización en la base de datos
                System.out.println("Pedido #" + pedidoSeleccionado.getIdPedido() + " actualizado a " + pedidoSeleccionado.getEstado());

                // Enviar correo al cliente
                emailService.enviarCorreoActualizacion(
                    pedidoSeleccionado.getUsuario().getEmail(),
                    pedidoSeleccionado.getUsuario().getNombre(),
                    pedidoSeleccionado.getIdPedido(),
                    pedidoSeleccionado.getEstado()
                );

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Correo enviado correctamente", 
                    "")
                );

            } catch (Exception e) {
                System.out.println("Error al actualizar pedido o enviar correo: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "No se pudo actualizar el estado del pedido o enviar el correo", 
                    "")
                );
            }
        }
    }

    public List<Compra> getListaPedidos() {
        return listaPedidos;
    }

}