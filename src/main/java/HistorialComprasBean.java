import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class HistorialComprasBean implements Serializable {
    private List<Compra> listaCompras;
    private List<Compra> comprasFiltradas;
    private String filtroIdPedido;
    private String filtroFecha;
    private String filtroEstado;

    @PostConstruct
    public void init() {
        cargarComprasDesdeERP();
    }

    private void cargarComprasDesdeERP() {
        listaCompras = new ArrayList<>();
        listaCompras.add(new Compra(101, "2025-03-01", 250.00, "Entregado"));
        listaCompras.add(new Compra(102, "2025-03-05", 100.00, "En Proceso"));
        listaCompras.add(new Compra(103, "2025-03-07", 75.50, "Cancelado"));
        listaCompras.add(new Compra(104, "2025-03-10", 300.75, "Entregado"));

        comprasFiltradas = new ArrayList<>(listaCompras);
    }

    public void filtrarCompras() {
        comprasFiltradas = listaCompras.stream()
                .filter(c -> (filtroIdPedido == null || filtroIdPedido.isEmpty()) || 
                             String.valueOf(c.getIdPedido()).contains(filtroIdPedido))
                .filter(c -> (filtroFecha == null || filtroFecha.isEmpty()) || 
                             c.getFecha().contains(filtroFecha))
                .filter(c -> (filtroEstado == null || filtroEstado.isEmpty()) || 
                             c.getEstado().toLowerCase().contains(filtroEstado.toLowerCase()))
                .collect(Collectors.toList());

        if (comprasFiltradas.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No se encontraron compras con los filtros aplicados", ""));
        }
    }

    // Getters y Setters
    public List<Compra> getComprasFiltradas() {
        return comprasFiltradas;
    }

    public String getFiltroIdPedido() {
        return filtroIdPedido;
    }

    public void setFiltroIdPedido(String filtroIdPedido) {
        this.filtroIdPedido = filtroIdPedido;
    }

    public String getFiltroFecha() {
        return filtroFecha;
    }

    public void setFiltroFecha(String filtroFecha) {
        this.filtroFecha = filtroFecha;
    }

    public String getFiltroEstado() {
        return filtroEstado;
    }

    public void setFiltroEstado(String filtroEstado) {
        this.filtroEstado = filtroEstado;
    }
}
