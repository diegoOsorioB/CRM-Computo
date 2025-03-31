import java.io.Serializable;

public class Compra implements Serializable {
    private int idPedido;    
    private String fecha;
    private double total;
    private String estado;
    private String estadoInicial;
    private User usuario;

    // Constructor sin parámetros (para evitar errores)
    public Compra() {
        this.idPedido = 0;
        this.fecha = "";
        this.total = 0.0;
        this.estado = "";
        this.estadoInicial = "";
        this.usuario = new User(); // Asegúrate de que User tenga un constructor vacío
    }
    
    public Compra(Pedido pedido) {
        this.idPedido = idPedido;
    }

    // Constructor sin usuario
    public Compra(int idPedido, String fecha, double total, String estado) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.estadoInicial = estado;
    }

    // Constructor con usuario
    public Compra(int idPedido, String fecha, double total, String estado, User usuario) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.estadoInicial = estado;
        this.usuario = usuario;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    @Override
    public String toString() {
        return "Compra{" +
                "idPedido=" + idPedido +
                ", fecha='" + fecha + '\'' +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", estadoInicial='" + estadoInicial + '\'' +
                ", usuario=" + (usuario != null ? usuario.toString() : "null") +
                '}';
    }

    Object getCarrito() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
