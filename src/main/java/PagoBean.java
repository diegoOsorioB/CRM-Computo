
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("pagoBean")
@SessionScoped
public class PagoBean implements Serializable {

    private List<ItemCarrito> items;
    private double total;

    @Inject
    private PedidoService pedidoService;
    @Inject
    private CarritoBean carritoBean;
    @Inject
    private PerfilData perfilData;

    public String procesarPago() {
        this.items = carritoBean.getItems();  // Asigna los productos del carrito
        this.total = carritoBean.getTotal();

        System.out.println("📌 Debug: Items en el carrito antes del pago: " + items);
        System.out.println("📌 Debug: Total antes del pago: " + total);

        if (items == null || items.isEmpty()) {
            System.out.println("❌ ERROR: No hay productos en el carrito.");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No hay productos en el carrito", null));
            return null;
        }

        System.out.println("✅ Carrito con productos: " + items.size());

        boolean pagoExitoso = enviarDatosAlERP();

        if (pagoExitoso) {
            System.out.println("✅ Pago exitoso. Agregando pedido...");
            agregarPedido();
            carritoBean.vaciarCarrito();
            generarComprobantePDF();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Pago realizado con éxito", null));

            return "Pedido.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en el pago", null));
            return null;
        }
    }

    private boolean enviarDatosAlERP() {
        // Simulación de pago aceptado
        return true;
    }

    private void agregarPedido() {
        String direccionCliente = perfilData.getStreet()+" "+perfilData.getCity(); // Obtener dirección del usuario logueado
    Pedido nuevoPedido = new Pedido(items, total, "En proceso", direccionCliente);
        pedidoService.agregarPedido(items, total,direccionCliente);
        
    System.out.println("🆔 Pedido agregado con ID: " + nuevoPedido.getId());
    }

    private void generarComprobantePDF() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String contenido = "Comprobante de Pago\nTotal: " + total;
            baos.write(contenido.getBytes());

            try (FileOutputStream fos = new FileOutputStream("comprobante.pdf")) {
                baos.writeTo(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
