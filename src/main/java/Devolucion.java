import java.io.Serializable;

public class Devolucion implements Serializable {
    private int idDevolucion;
    private Compra pedido;
    private String motivo;
    private String estatus;
    private String estatusInicial;
    private String razonCambioEstatus;    

    // Constructor sin parámetros (solución al error)
    public Devolucion() {
        // Valores por defecto o inicialización si es necesario
        this.idDevolucion = 0;
        this.pedido = new Compra(); // Asegúrate de que Compra tenga un constructor sin parámetros
        this.motivo = "";
        this.estatus = "";
        this.estatusInicial = "";
        this.razonCambioEstatus = "";
    }

    // Constructor con parámetros
    public Devolucion(int idDevolucion, Compra pedido, String motivo, String estatus, String razonCambioEstatus) {
        this.idDevolucion = idDevolucion;
        this.pedido = pedido;
        this.motivo = motivo;
        this.estatus = estatus;
        this.estatusInicial = estatus;
        this.razonCambioEstatus = razonCambioEstatus;
    }

    public int getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(int idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public Compra getPedido() {
        return pedido;
    }

    public void setPedido(Compra pedido) {
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
