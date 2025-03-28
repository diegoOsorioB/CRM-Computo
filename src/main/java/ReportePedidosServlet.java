import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/ReportePedidosServlet")
public class ReportePedidosServlet extends HttpServlet {
    private PedidoB pedidoController = new PedidoB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            pedidoController.consultarPedidos();
            List<Pedido> pedidos = pedidoController.getPedidos();
            
            // Reporte: Pedidos por estado
            Map<String, Long> pedidosPorEstado = pedidos.stream()
                    .collect(Collectors.groupingBy(Pedido::getEstado, Collectors.counting()));
            
            // Reporte: Ventas por mes
            Map<String, Double> ventasPorMes = new HashMap<>();
            for (Pedido pedido : pedidos) {
                String mes = Month.of(pedido.getFecha().getMonthValue()).name();
                ventasPorMes.put(mes, ventasPorMes.getOrDefault(mes, 0.0) + pedido.getTotal());
            }
            
            // Reporte: Pedidos por usuario
            Map<String, Long> pedidosPorUsuario = pedidos.stream()
                    .collect(Collectors.groupingBy(Pedido::getCorreoUsuario, Collectors.counting()));
            
            // Reporte: Ingresos totales
            double ingresosTotales = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
            
            // Construcción del JSON de respuesta
            Map<String, Object> reportes = new HashMap<>();
            reportes.put("pedidosPorEstado", pedidosPorEstado);
            reportes.put("ventasPorMes", ventasPorMes);
            reportes.put("pedidosPorUsuario", pedidosPorUsuario);
            reportes.put("ingresosTotales", ingresosTotales);
            
            out.print(jsonb.toJson(reportes));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error al generar reportes\"}");
        } finally {
            try {
                jsonb.close();
            } catch (Exception ex) {
                Logger.getLogger(ReportePedidosServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.flush();
        }
    }
}
