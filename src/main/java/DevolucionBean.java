
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Named
@ViewScoped
public class DevolucionBean implements Serializable {
    private List<Devolucion> listaDevoluciones;

    @Inject
    private EmailService emailService;

    @PostConstruct
    public void init() {
        cargarDevoluciones();
    }

    private void cargarDevoluciones() {
        listaDevoluciones = new ArrayList<>();
        listaDevoluciones.add(new Devolucion("201", new Compra(101, "2025-03-01", 3500.00, "Entregado", new User("1", "Juan", "Perez", "leopablo26@gmail.com",null)), "Producto defectuoso", "Pendiente", ""));
        listaDevoluciones.add(new Devolucion("202", new Compra(102, "2025-03-05", 500.00, "Entregado", new User("2", "Maria", "Lopez", "lopez@gmail.com",null)), "Talla incorrecta", "Pendiente", ""));
        listaDevoluciones.add(new Devolucion("203", new Compra(103, "2025-03-07", 400.00, "Entregado", new User("3", "Carlos", "Hernandez", "hernandez@gmail.com",null)), "No era lo que esperaba", "Pendiente", ""));        
    }

    public void actualizarEstatus(Devolucion devolucion) {    
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (devolucion.getRazonCambioEstatus() == null || devolucion.getRazonCambioEstatus().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Debe ingresar una razón para actualizar el pedido "+devolucion.getPedido().getIdPedido(), ""));
            return;
        } else if (devolucion.getEstatus().equals(devolucion.getEstatusInicial())){
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
        } else {
            try {            
                FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Estado actualizado:"+"El pedido #" + devolucion.getPedido().getIdPedido()+ " ahora está en estado: " + devolucion.getEstatus(),""));

                System.out.println("Devolución #" + devolucion.getIdDevolucion() + " actualizada a " + devolucion.getEstatus());

                emailService.enviarCorreoDevolucion(
                    devolucion.getPedido().getUsuario().getEmail(),
                    devolucion.getPedido().getUsuario().getNombre(),
                    devolucion.getIdDevolucion(),
                    devolucion.getEstatus(),
                    devolucion.getRazonCambioEstatus()
                );

                FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Correo enviado correctamente", ""));

                // Mensajes desaparecen al refrescar
                context.getExternalContext().getFlash().setKeepMessages(false);

            } catch (Exception e) {
                context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo actualizar el estado del pedido o enviar el correo", ""));
                e.printStackTrace();
            }
        }
    }

    public List<Devolucion> getListaDevoluciones() {
        return listaDevoluciones;
    }
}
