import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartOptions;

@Named
@ViewScoped
public class ReporteBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final float LEADING = 16;
    private static final Logger logger = Logger.getLogger(ReporteBean.class.getName());

    // Configuración de conexión
    private final String direccionIp = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app";
    private final String coleccion = "pedidos";
    private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";

    // Datos y estadísticas
    private List<Pedido> todosPedidos = new ArrayList<>();
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoFiltro;
    private double totalVentas;
    private int totalPedidos;
    private Map<String, Integer> ventasPorEstado = new HashMap<>();
    private Map<String, Double> ventasPorProducto = new HashMap<>();
    private boolean usandoDatosPrueba = false;
    private boolean conexionExitosa = false;
    
    // Modelos para gráficas
    private PieChartModel pieModel;
    private BarChartModel barModel;

    @PostConstruct
    public void init() {
        try {
            fechaInicio = LocalDate.now().withDayOfMonth(1);
            fechaFin = LocalDate.now();
            cargarDatos();
            crearGraficas();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en inicialización de ReporteBean", e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al inicializar el reporte", null));
        }
    }

    public void cargarDatos() {
        // Primero intentar conectar a la base de datos
        if (conectarBaseDatos()) {
            conexionExitosa = true;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Datos cargados correctamente desde la base de datos", null));
        } else {
            // Si falla, cargar datos de prueba
            cargarDatosDePrueba();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, 
                "No se pudo conectar a la base de datos. Mostrando datos de prueba", null));
        }
    }

    private boolean conectarBaseDatos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccion;
        Client client = null;

        try {
            client = ClientBuilder.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                
                if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                    throw new RuntimeException("La respuesta del servidor está vacía");
                }
                
                try (Jsonb jsonb = JsonbBuilder.create()) {
                    Pedido[] pedidosArray = jsonb.fromJson(jsonResponse, Pedido[].class);
                    
                    if (pedidosArray == null || pedidosArray.length == 0) {
                        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "No se encontraron pedidos en la base de datos", null));
                        return false;
                    }
                    
                    todosPedidos = Arrays.asList(pedidosArray);
                    usandoDatosPrueba = false;
                    generarEstadisticas();
                    return true;
                }
            } else {
                String errorMsg = "Error en el servicio: " + response.getStatus();
                try {
                    String responseBody = response.readEntity(String.class);
                    if (responseBody != null && !responseBody.isEmpty()) {
                        errorMsg += " - " + responseBody;
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "No se pudo obtener mensaje de error del servicio", e);
                }
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al conectar con la base de datos", e);
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void cargarDatosDePrueba() {
    try {
        logger.info("Cargando datos de prueba...");
        
        // Crear productos de prueba
        Producto producto1 = new Producto("1", "Laptop HP EliteBook", 18500.0);
        Producto producto2 = new Producto("2", "Mouse Logitech MX Master", 1250.0);
        Producto producto3 = new Producto("3", "Teclado mecánico Redragon", 2200.0);
        Producto producto4 = new Producto("4", "Monitor Samsung 24\"", 4500.0);
        Producto producto5 = new Producto("5", "Disco SSD 1TB", 1800.0);
        Producto producto6 = new Producto("6", "Impresora Laser HP", 3200.0);
        Producto producto7 = new Producto("7", "Webcam Logitech 1080p", 1500.0);
        Producto producto8 = new Producto("8", "Audífonos Sony", 2800.0);

        // Crear pedidos de prueba con datos variados
        todosPedidos = Arrays.asList(
            new Pedido("PED-001", Arrays.asList(
                new ItemCarrito(producto1, 1),
                new ItemCarrito(producto2, 1),
                new ItemCarrito(producto5, 2)
            ), 22300.0, "Completado", "Av. Reforma 150", "cliente1@empresa.com"),
            
            new Pedido("PED-002", Arrays.asList(
                new ItemCarrito(producto2, 3),
                new ItemCarrito(producto3, 1),
                new ItemCarrito(producto4, 2)
            ), 14750.0, "Pendiente", "Calle Juárez 45", "cliente2@mail.com"),
            
            new Pedido("PED-003", Arrays.asList(
                new ItemCarrito(producto1, 2),
                new ItemCarrito(producto4, 1),
                new ItemCarrito(producto8, 1)
            ), 47800.0, "Completado", "Blvd. López Mateos 1200", "cliente3@correo.com"),
            
            new Pedido("PED-004", Arrays.asList(
                new ItemCarrito(producto3, 1),
                new ItemCarrito(producto5, 3),
                new ItemCarrito(producto7, 2)
            ), 10400.0, "Cancelado", "Paseo de la Rosas 67", "cliente4@example.com"),
            
            new Pedido("PED-005", Arrays.asList(
                new ItemCarrito(producto2, 5),
                new ItemCarrito(producto3, 2),
                new ItemCarrito(producto4, 1),
                new ItemCarrito(producto5, 1)
            ), 19250.0, "Pendiente", "Calle Central 89", "cliente5@negocio.com"),
            
            new Pedido("PED-006", Arrays.asList(
                new ItemCarrito(producto6, 1),
                new ItemCarrito(producto7, 2)
            ), 6200.0, "Completado", "Av. Universidad 550", "cliente6@institucion.edu"),
            
            new Pedido("PED-007", Arrays.asList(
                new ItemCarrito(producto1, 1),
                new ItemCarrito(producto3, 1),
                new ItemCarrito(producto8, 2)
            ), 27700.0, "Completado", "Calle Morelos 12", "cliente7@servicio.com"),
            
            new Pedido("PED-008", Arrays.asList(
                new ItemCarrito(producto4, 3),
                new ItemCarrito(producto6, 1)
            ), 16700.0, "Pendiente", "Av. Revolución 345", "cliente8@empresa.net")
        );

        // Asignar fechas variadas para pruebas de filtrado
        todosPedidos.get(0).setFecha(LocalDate.now().minusDays(5));
        todosPedidos.get(1).setFecha(LocalDate.now().minusDays(3));
        todosPedidos.get(2).setFecha(LocalDate.now().minusDays(10));
        todosPedidos.get(3).setFecha(LocalDate.now().minusDays(15));
        todosPedidos.get(4).setFecha(LocalDate.now().minusDays(1));
        todosPedidos.get(5).setFecha(LocalDate.now().minusDays(7));
        todosPedidos.get(6).setFecha(LocalDate.now().minusDays(20));
        todosPedidos.get(7).setFecha(LocalDate.now().minusDays(2));

        // Generar estadísticas con los datos de prueba
        generarEstadisticas();
        
        logger.info("Datos de prueba cargados exitosamente. Total de pedidos: " + todosPedidos.size());
        
    } catch (Exception e) {
        logger.log(Level.SEVERE, "Error al cargar datos de prueba", e);
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_FATAL, 
            "Error crítico: No se pudieron cargar los datos de prueba", 
            "Por favor contacte al administrador del sistema."));
        
        // Asegurarse de que haya al menos una lista vacía para evitar NPEs
        todosPedidos = new ArrayList<>();
        ventasPorEstado = new HashMap<>();
        ventasPorProducto = new HashMap<>();
    }
}

    private void generarEstadisticas() {
        // Filtrar por rango de fechas
        List<Pedido> pedidosFiltrados = todosPedidos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> !p.getFecha().isBefore(fechaInicio) && !p.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());

        // Aplicar filtro de estado si existe
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            pedidosFiltrados = pedidosFiltrados.stream()
                    .filter(p -> estadoFiltro.equals(p.getEstado()))
                    .collect(Collectors.toList());
        }

        // Calcular totales
        totalPedidos = pedidosFiltrados.size();
        totalVentas = pedidosFiltrados.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();

        // Estadísticas por estado
        ventasPorEstado = pedidosFiltrados.stream()
                .collect(Collectors.groupingBy(
                        Pedido::getEstado,
                        Collectors.summingInt(p -> 1)
                ));

        // Estadísticas por producto
        ventasPorProducto = new HashMap<>();
        pedidosFiltrados.forEach(pedido -> {
            pedido.getItems().forEach(item -> {
                String nombreProducto = item.getProducto().getNombre();
                double totalProducto = item.getProducto().getPrecio() * item.getCantidad();
                ventasPorProducto.merge(nombreProducto, totalProducto, Double::sum);
            });
        });
        
        // Actualizar gráficas
        crearGraficas();
    }

    private void crearGraficas() {
        crearGraficaCircular();
        crearGraficaBarras();
    }

    private void crearGraficaCircular() {
    pieModel = new PieChartModel();
    ChartData data = new ChartData();

    PieChartDataSet dataSet = new PieChartDataSet();
    List<Number> values = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    List<String> bgColors = new ArrayList<>();

    // Colores para la gráfica
    String[] colores = {
        "rgb(255, 99, 132)", "rgb(54, 162, 235)", "rgb(255, 205, 86)",
        "rgb(75, 192, 192)", "rgb(153, 102, 255)", "rgb(255, 159, 64)"
    };

    // Agregar datos de ventas por estado
    int colorIndex = 0;
    for (Map.Entry<String, Integer> entry : ventasPorEstado.entrySet()) {
        values.add(entry.getValue());
        labels.add(entry.getKey());
        bgColors.add(colores[colorIndex % colores.length]);
        colorIndex++;
    }

    dataSet.setData(values);
    dataSet.setBackgroundColor(bgColors);
    data.addChartDataSet(dataSet);
    data.setLabels(labels);

    // Configuración de la gráfica
    pieModel.setData(data);

    // Crear opciones y agregar título
    PieChartOptions options = new PieChartOptions();
    Title title = new Title();
    title.setText("Distribución de Pedidos por Estado");
    title.setDisplay(true);
    
    options.setTitle(title);
    pieModel.setOptions(options); // Aquí se asigna correctamente
}

    private void crearGraficaBarras() {
    barModel = new BarChartModel();
    ChartData data = new ChartData();

    BarChartDataSet dataSet = new BarChartDataSet();
    dataSet.setLabel("Ventas por Producto");
    List<Number> values = new ArrayList<>();
    List<String> bgColors = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    // Colores para la gráfica
    String[] colores = {
        "rgb(54, 162, 235)", "rgb(255, 99, 132)", "rgb(255, 205, 86)",
        "rgb(75, 192, 192)", "rgb(153, 102, 255)", "rgb(255, 159, 64)"
    };

    // Agregar datos de ventas por producto
    int colorIndex = 0;
    for (Map.Entry<String, Double> entry : ventasPorProducto.entrySet()) {
        values.add(entry.getValue());
        labels.add(entry.getKey());
        bgColors.add(colores[colorIndex % colores.length]);
        colorIndex++;
    }

    dataSet.setData(values);
    dataSet.setBackgroundColor(bgColors);
    data.addChartDataSet(dataSet);
    data.setLabels(labels);

    // Configuración de la gráfica
    barModel.setData(data);

    // Crear opciones y agregar título
    BarChartOptions options = new BarChartOptions();
    Title title = new Title();
    title.setText("Ventas por Producto (en $)");
    title.setDisplay(true);
    
    options.setTitle(title);
    barModel.setOptions(options); // Aquí se asigna correctamente
}


    public void filtrarReporte() {
        generarEstadisticas();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Reporte actualizado", null));
    }

    public void generarReportePDF() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        if (todosPedidos == null || todosPedidos.isEmpty()) {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No hay datos para generar el reporte", ""));
            return;
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float yPosition = yStart;

                // Título del reporte
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("REPORTE DE PEDIDOS");
                contentStream.endText();
                yPosition -= LEADING * 2;

                // Gráfica circular de estados
                yPosition = agregarGraficaCircularPDF(document, contentStream, margin, yPosition);

                if (yPosition < margin + 300) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;
                }

                // Gráfica de barras de productos
                yPosition = agregarGraficaBarrasPDF(document, contentStream, margin, yPosition);

                if (yPosition < margin + 300) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;
                }

                // Estadísticas resumidas
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("ESTADÍSTICAS:");
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 15, yPosition);
                contentStream.showText("- Total de pedidos: " + totalPedidos);
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 15, yPosition);
                contentStream.showText("- Total de ventas: $" + totalVentas);
                contentStream.endText();
                yPosition -= LEADING * 1.5f;

                // Listado de pedidos
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("DETALLE DE PEDIDOS (" + todosPedidos.size() + "):");
                contentStream.endText();
                yPosition -= LEADING * 1.5f;

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                for (Pedido pedido : todosPedidos) {
                    if (yPosition < margin + (LEADING * 8)) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = yStart;
                    }

                    // Información del pedido
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Pedido: " + pedido.getId());
                    contentStream.endText();
                    yPosition -= LEADING;

                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 15, yPosition);
                    contentStream.showText("- Fecha: " + pedido.getFecha());
                    contentStream.endText();
                    yPosition -= LEADING;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 15, yPosition);
                    contentStream.showText("- Estado: " + pedido.getEstado());
                    contentStream.endText();
                    yPosition -= LEADING;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 15, yPosition);
                    contentStream.showText("- Total: $" + pedido.getTotal());
                    contentStream.endText();
                    yPosition -= LEADING;

                    // Productos
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 15, yPosition);
                    contentStream.showText("- Productos:");
                    contentStream.endText();
                    yPosition -= LEADING;

                    for (ItemCarrito item : pedido.getItems()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 30, yPosition);
                        contentStream.showText("• " + item.getCantidad() + " x " + item.getProducto().getNombre()
                                + " ($" + item.getProducto().getPrecio() + " c/u)");
                        contentStream.endText();
                        yPosition -= LEADING;
                    }

                    yPosition -= LEADING;
                }

            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);

            externalContext.responseReset();
            externalContext.setResponseContentType("application/pdf");
            externalContext.setResponseContentLength(baos.size());
            externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"reporte_pedidos.pdf\"");

            try (OutputStream output = externalContext.getResponseOutputStream()) {
                output.write(baos.toByteArray());
                output.flush();
            }

            facesContext.responseComplete();

        } catch (Exception e) {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al generar PDF", e.getMessage()));
            e.printStackTrace();
        }
    }

    private float agregarGraficaCircularPDF(PDDocument document, PDPageContentStream contentStream,
            float margin, float yPosition) throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        ventasPorEstado.forEach((estado, cantidad) -> dataset.setValue(estado, cantidad));

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Pedidos por Estado",
                dataset,
                true, true, false);

        BufferedImage bufferedImage = chart.createBufferedImage(500, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        PDImageXObject image = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "chart");

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Distribución por Estado:");
        contentStream.endText();
        yPosition -= LEADING;

        contentStream.drawImage(image, margin, yPosition - 300, 500, 300);
        return yPosition - 320;
    }

    private float agregarGraficaBarrasPDF(PDDocument document, PDPageContentStream contentStream,
            float margin, float yPosition) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ventasPorProducto.forEach((producto, total) -> dataset.addValue(total, "Ventas", producto));

        JFreeChart chart = ChartFactory.createBarChart(
                "Ventas por Producto",
                "Productos",
                "Monto ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);

        BufferedImage bufferedImage = chart.createBufferedImage(500, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        PDImageXObject image = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "chart");

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Ventas por Producto:");
        contentStream.endText();
        yPosition -= LEADING;

        contentStream.drawImage(image, margin, yPosition - 300, 500, 300);
        return yPosition - 320;
    }

    // Getters y Setters
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        this.estadoFiltro = estadoFiltro;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public Map<String, Integer> getVentasPorEstado() {
        return ventasPorEstado;
    }

    public Map<String, Double> getVentasPorProducto() {
        return ventasPorProducto;
    }

    public List<Pedido> getTodosPedidos() {
        return todosPedidos;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public BarChartModel getBarModel() {
        return barModel;
    }
}