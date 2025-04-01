import java.io.Serializable;

public class Devolucion implements Serializable {
    private String idDevolucion;
    private Pedido pedido;
    private String motivo;
    private String estatus;
    private String estatusInicial;
    private String razonCambioEstatus;    

    public Devolucion(String idDevolucion, Pedido pedido, String motivo, String estatus, String razonCambioEstatus) {
        this.idDevolucion = idDevolucion;
        this.pedido = pedido;
        this.motivo = motivo;
        this.estatus = estatus;
        this.estatusInicial = estatus;
        this.razonCambioEstatus = razonCambioEstatus;
    }
    
    public Devolucion(){
        
    }

    public String getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(String idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getEstatusInicial() {
        return estatusInicial;
    }

    public void setEstatusInicial(String estatusInicial) {
        this.estatusInicial = estatusInicial;
    }

    public String getRazonCambioEstatus() {
        return razonCambioEstatus;
    }

    public void setRazonCambioEstatus(String razonCambioEstatus) {
        this.razonCambioEstatus = razonCambioEstatus;
    }

    @Override
    public String toString() {
        return "Devolucion{" +
                "idDevolucion=" + idDevolucion +
                ", pedido=" + pedido +
                ", motivo='" + motivo + '\'' +
                ", estatus='" + estatus + '\'' +
                ", estatusInicial='" + estatusInicial + '\'' +
                ", razonCambioEstatus='" + razonCambioEstatus + '\'' +
                '}';
    }
}
