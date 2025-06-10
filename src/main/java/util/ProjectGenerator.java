package util;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class ProyectGenerator {
    private String targetOutputPath;
    private String projectName;
    private final String SpringBootUrl = " https://start.spring.io/starter.zip";
    public ProyectGenerator(String targetOutputPath, String projectName) {
        this.targetOutputPath = targetOutputPath;
        this.projectName = projectName;
    }
    public boolean generarSpringProyect(){
        try{
            String params = "dependencies=web,data-jpa,h2,lombok" +
                "&type=maven-project" +
                "&language=java" +
                "&name=" + this.projectName +
                "&groupId=com.example" +
                "&artifactId=" + this.projectName +
                "&packageName=com.example." + this.projectName +
                "&javaVersion=17";
            URL url = new URL(this.SpringBootUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = params.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("Error: " + responseCode);
                    return false;
                }

                // Read the response
                InputStream inputStream = connection.getInputStream();
                File outputFile = new File(this.targetOutputPath, this.projectName + ".zip");
                try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            } finally {
                connection.disconnect();
            }
            unZipProject();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean unZipProject(){
        try {
            File zipFile = new File(this.targetOutputPath, this.projectName + ".zip");
            if (!zipFile.exists()) {
                System.out.println("El archivo ZIP no existe: " + zipFile.getAbsolutePath());
                return false;
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(this.targetOutputPath + "/" + this.projectName, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
            zis.close();
            //Remove the zip file after extraction
            if (zipFile.delete()) {
                System.out.println("Archivo ZIP eliminado: " + zipFile.getAbsolutePath());
            } else {
                System.out.println("No se pudo eliminar el archivo ZIP: " + zipFile.getAbsolutePath());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
