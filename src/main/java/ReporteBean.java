import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

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

    // Método para generar el reporte en PDF
    public void generarReportePDF(Reportes reporte) {
        // Nombre del archivo PDF a generar
        String fileName = "reporte_" + reporte.getPedido().getIdPedido() + ".pdf";

        // Ruta del archivo PDF (puedes personalizarla)
        String rutaArchivo = "C:/reports/" + fileName;

        try {
            // Crear PdfWriter (la salida donde se guardará el archivo PDF)
            PdfWriter writer = new PdfWriter(rutaArchivo);

            // Crear PdfDocument (el documento que vamos a construir)
            PdfDocument pdf = new PdfDocument(writer);

            // Crear el documento PDF (esto es lo que vas a ver como contenido)
            Document document = new Document(pdf);

            // Agregar título al reporte
            document.add(new Paragraph("Reporte de Pedido #" + reporte.getPedido().getIdPedido())
                    .setFont(com.itextpdf.layout.font.FontProgramFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(18));

            // Agregar detalles del pedido
            document.add(new Paragraph("Detalles del Pedido:")
                    .setFont(com.itextpdf.layout.font.FontProgramFactory.createFont(StandardFonts.HELVETICA))
                    .setFontSize(12));

            // Agregar detalles del cliente
            document.add(new Paragraph("Cliente: " + reporte.getPedido().getUsuario().getNombre() + " " + 
                                       reporte.getPedido().getUsuario().getApellidoPaterno()));
            document.add(new Paragraph("Total: $" + reporte.getPedido().getTotal()));

            // Agregar productos del carrito
            for (Producto producto : reporte.getPedido().getCarrito().getProductos()) {
                document.add(new Paragraph("Producto: " + producto.getNombre() + 
                                           " - Precio: $" + producto.getPrecio() + 
                                           " - Cantidad: " + producto.getCantidad()));
            }

            // Cerrar el documento
            document.close();

            // Notificar al usuario que el reporte ha sido generado correctamente
            System.out.println("Reporte en PDF generado exitosamente: " + fileName);

        } catch (Exception e) {
            // En caso de error, mostrar un mensaje de error y logear la excepción
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo generar el reporte en PDF", ""));
            e.printStackTrace();
        }
    }
}
