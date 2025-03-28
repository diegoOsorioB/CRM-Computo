import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class ReporteBean implements Serializable {

    private List<Reportes> listaReportes;
    private List<Devolucion> listaDevoluciones;

    @Inject
    private EmailService emailService;

    @PostConstruct
    public void init() {
        cargarReportes();
    }
    
    // Constructor
    public ReporteBean() {
        listaDevoluciones = new ArrayList<>();
    }

    private void cargarReportes() {
        listaReportes = new ArrayList<>();
        listaDevoluciones.add(new Devolucion(201, new Compra(101, "2025-03-01", 3500.00, "Entregado", new User("1", "Juan", "Perez", "leopablo26@gmail.com", "cliente")), "Producto defectuoso", "Pendiente", ""));
    }

    public void actualizarEstatus(Reportes reporte) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (reporte.getRazonCambioEstatus() == null || reporte.getRazonCambioEstatus().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Debe ingresar una razón para actualizar el reporte del pedido #" + reporte.getPedido().getIdPedido(), ""));
            return;
        } else if (reporte.getEstatus().equals(reporte.getEstatusInicial())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
        } else {
            try {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Estado actualizado: El pedido #" + reporte.getPedido().getIdPedido() + " ahora está en estado: " + reporte.getEstatus(), ""));

                System.out.println("Reporte #" + reporte.getIdDevolucion() + " actualizado a " + reporte.getEstatus());

                // Enviar correo con la actualización del reporte
                emailService.enviarCorreoDevolucion(
                    reporte.getPedido().getUsuario().getEmail(),
                    reporte.getPedido().getUsuario().getNombre(),
                    reporte.getIdDevolucion(),
                    reporte.getEstatus(),
                    reporte.getRazonCambioEstatus()
                );

                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Correo enviado correctamente", ""));

                // Mantener los mensajes de flash
                context.getExternalContext().getFlash().setKeepMessages(true);

            } catch (Exception e) {
                context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo actualizar el estado del reporte o enviar el correo", ""));
                e.printStackTrace();
            }
        }
    }

    public List<Reportes> getListaReportes() {
        return listaReportes;
    }

    // Método para generar el reporte en archivo de texto
    public void generarReporteTexto(Reportes reporte) {
        // Nombre del archivo de texto a generar
        String fileName = "reporte_" + reporte.getPedido().getIdPedido() + ".txt";

        // Ruta del archivo de texto (puedes personalizarla)
        String rutaArchivo = "C:/reports/" + fileName;

        try {
            // Crear un archivo de texto
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                archivo.createNewFile(); // Si no existe, crear el archivo
            }

            // Crear un BufferedWriter para escribir en el archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
                // Escribir título del reporte
                writer.write("Reporte de Pedido #" + reporte.getPedido().getIdPedido() + "\n");
                writer.write("===============================\n\n");

                // Agregar detalles del pedido
                writer.write("Detalles del Pedido:\n");
                writer.write("Cliente: " + reporte.getPedido().getUsuario().getNombre() + " " + 
                             reporte.getPedido().getUsuario().getApellidoPaterno() + "\n");
                writer.write("Total: $" + reporte.getPedido().getTotal() + "\n\n");

                // Agregar productos del carrito
                writer.write("Productos:\n");
                for (Producto producto : reporte.getPedido().getCarrito().getProductos()) {
                    writer.write("Producto: " + producto.getNombre() + " - Precio: $" + producto.getPrecio() + 
                                 " - Cantidad: " + producto.getCantidad() + "\n");
                }

                writer.write("\nReporte generado exitosamente.");
            }

            // Notificar al usuario que el reporte ha sido generado correctamente
            System.out.println("Reporte en texto generado exitosamente: " + fileName);

        } catch (IOException e) {
            // En caso de error, mostrar un mensaje de error y logear la excepción
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo generar el reporte en archivo de texto", ""));
            e.printStackTrace();
        }
    }
}
