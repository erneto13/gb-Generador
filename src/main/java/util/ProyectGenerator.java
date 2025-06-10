package util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProyectGenerator {
    private final String targetOutputPath;
    private final String projectName;

    public ProyectGenerator(String targetOutputPath, String projectName) {
        this.targetOutputPath = targetOutputPath;
        this.projectName = projectName;
    }

    public boolean generarSpringProyect() {
        try {
            Path outputDirectory = Paths.get(this.targetOutputPath);
            if (!Files.exists(outputDirectory)) {
                try {
                    Files.createDirectories(outputDirectory);
                    System.out.println("Directorio de salida creado: " + outputDirectory.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Fallo al crear el directorio de salida: " + outputDirectory.toAbsolutePath() + " - " + e.getMessage());
                    return false;
                }
            }

            HttpURLConnection connection = getHttpURLConnection();
            Path outputFile = Paths.get(this.targetOutputPath, this.projectName + ".zip");
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("Error en la respuesta de Spring Initializr: " + responseCode);
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        String line;
                        System.err.println("Spring Initializr Error Details:");
                        while ((line = errorReader.readLine()) != null) {
                            System.err.println(line);
                        }
                    }
                    return false;
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
                }
                System.out.println("Archivo ZIP de Spring Boot descargado en: " + outputFile.toAbsolutePath());
            } finally {
                connection.disconnect();
            }
            unZipProject();
            return true;
        } catch (Exception e) {
            System.err.println("Excepción al generar proyecto Spring Boot: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private HttpURLConnection getHttpURLConnection() throws IOException {
        String params = "dependencies=web,data-jpa,h2,lombok" +
                "&type=maven-project" +
                "&language=java" +
                "&name=" + this.projectName +
                "&groupId=com.app" +
                "&artifactId=" + this.projectName +
                "&packageName=com.app." + this.projectName.toLowerCase() +
                "&javaVersion=17";
        String springBootUrl = "https://start.spring.io/starter.zip";
        URL url = new URL(springBootUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return connection;
    }

    public void unZipProject() {
        try {
            Path zipFile = Paths.get(this.targetOutputPath, this.projectName + ".zip");
            if (!Files.exists(zipFile)) {
                System.out.println("El archivo ZIP no existe para descomprimir: " + zipFile.toAbsolutePath());
                return;
            }

            unzipFileContentFlattened(zipFile);

            try {
                Files.delete(zipFile);
                System.out.println("Archivo ZIP eliminado: " + zipFile.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("No se pudo eliminar el archivo ZIP: " + zipFile.toAbsolutePath() + " - " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Excepción al descomprimir el proyecto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void unzipFileContentFlattened(Path zipFile) throws IOException {
        Path destDir = Paths.get(this.targetOutputPath, this.projectName); // Aquí es donde debería residir el proyecto

        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }

        String rootDirToSkip = null;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                String entryName = entry.getName();
                int firstSlash = entryName.indexOf("/");
                if (firstSlash != -1) {
                    rootDirToSkip = entryName.substring(0, firstSlash + 1);
                }
                if (entry.isDirectory() && !rootDirToSkip.endsWith("/")) {
                    rootDirToSkip += "/";
                }
            }
        }

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                String adjustedPath = entryName;

                if (rootDirToSkip != null && entryName.startsWith(rootDirToSkip)) {
                    adjustedPath = entryName.substring(rootDirToSkip.length());
                }

                if (adjustedPath.isEmpty()) {
                    continue;
                }

                Path newFilePath = destDir.resolve(adjustedPath);

                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                    System.out.println("Directorio creado: " + newFilePath.toAbsolutePath());
                } else {
                    Path parentDir = newFilePath.getParent();
                    if (parentDir != null) {
                        Files.createDirectories(parentDir);
                    }
                    Files.copy(zis, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Archivo extraído: " + newFilePath.toAbsolutePath());
                }
            }
        }

        System.out.println("Proyecto Spring Boot descomprimido con estructura correcta en: " +
                destDir.toAbsolutePath());
    }

    public boolean generarAngularProyect(String outputDir, String projectName) {
        if (!isAngularCliInstalled()) {
            System.err.println("Angular CLI no está instalado o no está disponible en el PATH.");
            System.err.println("Por favor instala Angular CLI ejecutando: npm install -g @angular/cli");
            System.err.println("No se puede generar el proyecto Angular: " + projectName);
            return false;
        }

        try {
            Path baseOutput = Paths.get(outputDir);
            if (!Files.exists(baseOutput)) {
                Files.createDirectories(baseOutput);
            }

            String ngCommand = getAngularCliCommand();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    ngCommand, "new", projectName,
                    "--skip-install",
                    "--defaults",
                    "--routing=true",
                    "--style=css"
            );
            processBuilder.directory(baseOutput.toFile());

            System.out.println("Ejecutando comando: " + ngCommand + " new " + projectName + " en " + baseOutput.toAbsolutePath());
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("NG-CLI Output: " + line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println("NG-CLI ERROR: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Comando 'ng new' ejecutado con éxito. Código de salida: " + exitCode);
                return true;
            } else {
                System.err.println("El comando 'ng new' falló. Código de salida: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error al ejecutar el comando 'ng new': " + e.getMessage());
            return false;
        }
    }

    private boolean isAngularCliInstalled() {
        try {
            String ngCommand = getAngularCliCommand();
            ProcessBuilder processBuilder = new ProcessBuilder(ngCommand, "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String getAngularCliCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "ng.cmd";
        } else {
            return "ng";
        }
    }
}