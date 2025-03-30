
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    @Inject
    private PedidoB pedidoB;

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
            try {
                agregarPedido();
            } catch (Exception e) {
                System.out.println("Ocurrio un error "+e);
            }
            generarComprobantePDF();
            carritoBean.vaciarCarrito();
            

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Pago realizado con éxito", null));

            return "Product.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en el pago", null));
            return null;
        }
    }

    private boolean enviarDatosAlERP() {
      /*  try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"nombre\":\"%s\",\"correo\":\"%s\",\"tarjeta\":\"%s\"}",
                    perfilData.getNombre(), perfilData.getEmail(), perfilData.getNumCuenta());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/ApiERP/api/pagos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            System.out.println("Error al enviar datos al ERP: " + e.getMessage());
            return false;
        }*/
      return true;
    }

    private void agregarPedido() throws Exception {
    FacesContext context = FacesContext.getCurrentInstance();
    String emailUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");
        System.out.println(emailUsuario+"++++++++++++++-------------");

    if (emailUsuario == null || emailUsuario.isEmpty()) {
        System.out.println("❌ ERROR: No se encontró el email del usuario en la sesión.");
        return;
    }

    String direccionCliente = perfilData.getDireccion() + " " + perfilData.getCiudad();
    Pedido nuevoPedido = new Pedido(null,items, total, "En proceso", direccionCliente, emailUsuario);

    pedidoService.agregarPedido(items, total, direccionCliente);

    System.out.println("🆔 Pedido agregado con ID: " + nuevoPedido.getId());
    

    pedidoB.setPedido(nuevoPedido);
    pedidoB.insertarPedido();
}


  private void generarComprobantePDF() {
    try {
        // 📌 Obtener la carpeta de descargas del usuario
        String userHome = System.getProperty("user.home");
        String downloadsPath = Paths.get(userHome, "Downloads", "Comprobante_Pago.pdf").toString();
        
        // 📌 Asegurar que la carpeta de descargas exista
        File downloadsDir = new File(Paths.get(userHome, "Downloads").toString());
        if (!downloadsDir.exists()) {
            System.out.println("? La carpeta 'Downloads' no existe. Creándola...");
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
        String direccionCliente = perfilData.getDireccion() + " " + perfilData.getCiudad();
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
