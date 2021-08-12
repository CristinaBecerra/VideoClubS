
package videoclubs;

import java.sql.Connection;

public class DatosConn {
    private Connection conn;
    private String codigoError;
    private String mensajeError;

    //---------------------------------------------------------------
    public DatosConn(){
        conn = null;
        codigoError = "";
        mensajeError = "";
    };
    
    public DatosConn(Connection conn, String codigoError, String mensajeError) {
        this.conn = conn;
        this.codigoError = codigoError;
        this.mensajeError = mensajeError;
    }

    //--------------------Getters y Setters-------------------------------------
    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }
    
    //--------------------------------------------------------------------------

    @Override
    public String toString() {
        return codigoError + " : " + mensajeError;
    }
    
    
}
