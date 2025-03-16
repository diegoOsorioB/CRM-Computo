
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
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
            generarComprobantePDF();
            carritoBean.vaciarCarrito();
            

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
    try {
        // 📌 Obtener la carpeta de descargas del usuario
        String userHome = System.getProperty("user.home");
        String downloadsPath = Paths.get(userHome, "Downloads", "Comprobante_Pago.pdf").toString();
        
        // 📌 Asegurar que la carpeta de descargas exista
        File downloadsDir = new File(Paths.get(userHome, "Downloads").toString());
        if (!downloadsDir.exists()) {
            System.out.println("📁 La carpeta 'Downloads' no existe. Creándola...");
            boolean creada = downloadsDir.mkdirs();
            if (!creada) {
                System.out.println("❌ ERROR: No se pudo crear la carpeta de descargas.");
                return;
            }
        }

        // 📌 Crear el archivo PDF
        PdfWriter writer = new PdfWriter(new FileOutputStream(downloadsPath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 📌 Agregar título
        document.add(new Paragraph("Comprobante de Pago").setBold().setFontSize(18));
        document.add(new Paragraph("Total: $" + total).setBold());

        // 📌 Crear la tabla de productos
        Table table = new Table(new float[]{3, 1, 1});
        table.addCell(new Cell().add(new Paragraph("Producto").setBold()));
        table.addCell(new Cell().add(new Paragraph("Cantidad").setBold()));
        table.addCell(new Cell().add(new Paragraph("Subtotal").setBold()));

        // 📌 Verificar si hay productos en la lista
        if (items != null && !items.isEmpty()) {
            for (ItemCarrito item : items) {
                if (item.getProducto() != null) { // Verificar que el producto no sea nulo
                    table.addCell(new Cell().add(new Paragraph(item.getProducto().getNombre())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getCantidad()))));
                    table.addCell(new Cell().add(new Paragraph("$" + (item.getProducto().getPrecio() * item.getCantidad()))));
                } else {
                    table.addCell(new Cell().add(new Paragraph("Producto no disponible")));
                    table.addCell(new Cell().add(new Paragraph("-")));
                    table.addCell(new Cell().add(new Paragraph("-")));
                }
            }
        } else {
            table.addCell(new Cell(1, 3).add(new Paragraph("No hay productos en el carrito.")));
        }

        document.add(table);

        // 📌 Agregar dirección de envío
        String direccionCliente = perfilData.getStreet() + " " + perfilData.getCity();
        document.add(new Paragraph("Dirección de Envío: " + direccionCliente));

        document.close();
        System.out.println("✅ PDF generado correctamente en: " + downloadsPath);

    } catch (IOException e) {
        System.out.println("❌ ERROR al generar el PDF:");
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
