import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class ArchivosPDF {

    private static final String API_BASE_URL = "https://apigateway.nicepebble-44974112.eastus.azurecontainerapps.io";
    private static final String USERNAME = "K0RS3P";
    private static final String PASSWORD = "P3rzival20!";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String subirPDF(ByteArrayOutputStream pdfStream, String nombreArchivo) {
        try {
            // 1. Autenticación para obtener el JWT
            String jwt = obtenerJWT();
            if (jwt == null) {
                return "Error al autenticar con el servidor de archivos";
            }

            // 2. Subir el archivo PDF
            byte[] pdfBytes = pdfStream.toByteArray();
            String fileId = subirArchivo(pdfBytes, nombreArchivo, jwt);
            
            if (fileId != null) {
                return "PDF compartido exitosamente. ID del archivo: " + fileId;
            } else {
                return "Error al subir el PDF al servidor";
            }
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al compartir PDF", e.getMessage()));
            e.printStackTrace();
            return "Error al compartir PDF: " + e.getMessage();
        }
    }

    private String obtenerJWT() throws IOException, InterruptedException {
        JSONObject authRequest = new JSONObject();
        authRequest.put("username", USERNAME);
        authRequest.put("password", PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(authRequest.toString()))
                .uri(URI.create(API_BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("token");
        } else {
            throw new RuntimeException("Error en autenticación: " + response.body());
        }
    }

    private String subirArchivo(byte[] fileBytes, String fileName, String jwt) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        // Crear el cuerpo multipart
        byte[] filePart = ("--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/pdf\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        
        byte[] requestBody = new byte[filePart.length + fileBytes.length + footer.length];
        System.arraycopy(filePart, 0, requestBody, 0, filePart.length);
        System.arraycopy(fileBytes, 0, requestBody, filePart.length, fileBytes.length);
        System.arraycopy(footer, 0, requestBody, filePart.length + fileBytes.length, footer.length);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .uri(URI.create(API_BASE_URL + "/files/upload"))
                .header("Authorization", "Bearer " + jwt)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("id");
        } else {
            throw new RuntimeException("Error al subir archivo: " + response.body());
        }
    }
}