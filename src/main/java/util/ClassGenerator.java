package util;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClassGenerator {

    private static final String OUTPUT_DIR = "output";
    private static final String TEMP_JAVA_FILES_DIR = "tempJavaFiles";
    private static final String TEMP_TS_FILES_DIR = "tempTsFiles";

    private static final String ANGULAR_PROJECT_SRC_PATH = "src/app/";

    private static final String TEMPLATES_DIR = "src/main/resources/templates/";

    private static final Map<String, String> JAVA_TEMPLATES_TO_PACKAGES = Map.of(
            "ClassTemplate.vm", "models",
            "ControllerTemplate.vm", "controllers",
            "RepositoryTemplate.vm", "repositories",
            "ServiceTemplate.vm", "services",
            "NonEntityClassTemplate.vm", "models"
    );

    private static final Map<String, String> TS_TEMPLATES_TO_PACKAGES = Map.of(
            "TsModelTemplate.vm", "interfaces",
            "TsServiceTemplate.vm", "services"
    );

    private static final Map<String, String> SUFFIX_MAP = Map.of(
            "controllers", "Controller",
            "repositories", "Repository",
            "services", "Service"
    );

    private final JSONArray jsonArray;

    public ClassGenerator(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public void generate() {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject projectObject = jsonArray.getJSONObject(i);
            String projectName = projectObject.getString("projectName");
            String javaProjectName = projectName + "Backend";
            String angularProjectName = projectName + "Frontend";

            ProyectGenerator javaProjectGenerator = new ProyectGenerator(OUTPUT_DIR, javaProjectName);
            System.out.println("GENERANDO PROYECTO SPRING :: " + javaProjectName);
            if (javaProjectGenerator.generarSpringProyect()) {
                System.out.println("PROYECTO GENERADO :: '" + javaProjectName);
            } else {
                System.err.println("ERROR AL GENERAR :: " + javaProjectName);
                continue;
            }

            ProyectGenerator angularProjectGenerator = new ProyectGenerator(OUTPUT_DIR, angularProjectName);
            System.out.println("Generando proyecto Angular: " + angularProjectName);
            if (angularProjectGenerator.generarAngularProyect(OUTPUT_DIR, angularProjectName)) {
                System.out.println("Proyecto Angular '" + angularProjectName + "' generado exitosamente.");
            } else {
                System.err.println("Fallo al generar el proyecto Angular: " + angularProjectName);
                continue;
            }

            String tempJavaPath = OUTPUT_DIR + "/" + TEMP_JAVA_FILES_DIR;
            String tempTsPath = OUTPUT_DIR + "/" + TEMP_TS_FILES_DIR;

            deleteDirectory(new File(tempJavaPath));
            deleteDirectory(new File(tempTsPath));

            generatePackage(tempTsPath + "/interfaces");
            generatePackage(tempTsPath + "/services");

            List<JSONObject> currentProjectEntityClasses = new ArrayList<>();

            String basePackageForJava = "com.app." + javaProjectName.toLowerCase();

            if (projectObject.has("package")) {
                JSONArray packages = projectObject.getJSONArray("package");
                for (int j = 0; j < packages.length(); j++) {
                    JSONObject packageObj = packages.getJSONObject(j);
                    String packageNameFromConfig = packageObj.getString("packageName");

                    JSONArray classes = packageObj.getJSONArray("classes");
                    for (int k = 0; k < classes.length(); k++) {
                        JSONObject currentClass = classes.getJSONObject(k);
                        if (currentClass.optBoolean("isEntity", false)) {
                            currentProjectEntityClasses.add(currentClass);
                        }
                        generateClass(currentClass, basePackageForJava, packageNameFromConfig, tempJavaPath, tempTsPath, null, projectName);
                    }
                }
            }

            if (projectObject.has("class")) {
                JSONObject classObj = projectObject.getJSONObject("class");
                if (classObj.optBoolean("isEntity", false)) {
                    currentProjectEntityClasses.add(classObj);
                }
                generateClass(classObj, basePackageForJava, "models", tempJavaPath, tempTsPath, null, projectName);
            }

            String javaProjectSrcRoot = Paths.get(OUTPUT_DIR, javaProjectName, "src", "main", "java").toString();
            String angularTargetBasePath = Paths.get(OUTPUT_DIR, angularProjectName, ANGULAR_PROJECT_SRC_PATH).toString();

            moveGeneratedFiles(tempJavaPath, javaProjectSrcRoot);
            moveGeneratedFiles(tempTsPath, angularTargetBasePath);

            deleteDirectory(new File(tempJavaPath));
            deleteDirectory(new File(tempTsPath));
        }
    }

    public void generatePackage(String fullPath) {
        File packageDir = new File(fullPath);
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
    }

    public void generateClass(JSONObject classe, String basePackageForJava, String subPackageNameFromConfig, String tempJavaPath, String tempTsPath, String tempSqlPath, String projectName) {
        try {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
            velocityEngine.setProperty("file.resource.loader.path", TEMPLATES_DIR);
            velocityEngine.init();

            generateJavaFiles(velocityEngine, classe, basePackageForJava, subPackageNameFromConfig, tempJavaPath, projectName);
            generateTsFiles(velocityEngine, classe, tempTsPath, projectName);

        } catch (Exception e) {
            System.err.println("Error al generar la clase para " + classe.optString("className", "desconocida") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateJavaFiles(VelocityEngine velocityEngine, JSONObject classe, String basePackageForJava, String subPackageNameFromConfig, String tempJavaPath, String projectName) throws Exception {
        String className = classe.getString("className");
        boolean isEntity = classe.optBoolean("isEntity", false);

        Map<String, String> templatesToProcess = new HashMap<>();
        if (isEntity) {
            templatesToProcess.put("ClassTemplate.vm", "models");
            templatesToProcess.put("ControllerTemplate.vm", "controllers");
            templatesToProcess.put("RepositoryTemplate.vm", "repositories");
            templatesToProcess.put("ServiceTemplate.vm", "services");
        } else {
            templatesToProcess.put("NonEntityClassTemplate.vm", "models");
        }

        for (Map.Entry<String, String> entry : templatesToProcess.entrySet()) {
            String templateName = entry.getKey();
            String packageTarget = entry.getValue();

            Template jTemplate = velocityEngine.getTemplate(templateName);
            Context context = new org.apache.velocity.VelocityContext();
            StringWriter writer = new StringWriter();

            String fileName = className;
            if (!templateName.equals("ClassTemplate.vm") && !templateName.equals("NonEntityClassTemplate.vm")) {
                String suffix = SUFFIX_MAP.getOrDefault(packageTarget, "");
                fileName += suffix;
            }

            String fullJavaPackage;
            if (subPackageNameFromConfig != null && !subPackageNameFromConfig.isEmpty() && !"models".equalsIgnoreCase(subPackageNameFromConfig)) {
                fullJavaPackage = basePackageForJava + "." + subPackageNameFromConfig + "." + packageTarget;
            } else if (subPackageNameFromConfig != null && "models".equalsIgnoreCase(subPackageNameFromConfig) && !packageTarget.equalsIgnoreCase("models")) {
                fullJavaPackage = basePackageForJava + "." + subPackageNameFromConfig + "." + packageTarget;
            } else {
                fullJavaPackage = basePackageForJava + "." + packageTarget;
            }

            String relativePathInTemp = fullJavaPackage.replace(".", File.separator) + File.separator + fileName + ".java";
            String fullPathInTemp = Paths.get(tempJavaPath, relativePathInTemp).toString();
            File file = new File(fullPathInTemp);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();

                Map<String, Object> classMap = new HashMap<>(classe.toMap());
                classMap.put("idType", determineIdType(classe, false));

                context.put("class", classMap);
                context.put("projectName", projectName);
                context.put("packageName", fullJavaPackage);
                context.put("packageTarget", packageTarget);

                jTemplate.merge(context, writer);
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(writer.toString());
                }
            }
        }
    }

    private void generateTsFiles(VelocityEngine velocityEngine, JSONObject classe, String tempTsPath, String projectName) throws Exception {
        String className = classe.getString("className");

        for (Map.Entry<String, String> entry : TS_TEMPLATES_TO_PACKAGES.entrySet()) {
            String templateName = entry.getKey();
            String packageTarget = entry.getValue();

            Template tsTemplate = velocityEngine.getTemplate(templateName);
            Context context = new org.apache.velocity.VelocityContext();
            StringWriter writer = new StringWriter();

            String tsFileName = className;
            if (templateName.equals("TsModelTemplate.vm")) {
                tsFileName = "I" + tsFileName;
            } else if (templateName.equals("TsServiceTemplate.vm")) {
                tsFileName += "Service";
            }

            String fullPath = Paths.get(tempTsPath, packageTarget, tsFileName + ".ts").toString();
            File file = new File(fullPath);

            if (!file.exists()) {
                file.createNewFile();

                Map<String, Object> classMapForTs = new HashMap<>(classe.toMap());
                classMapForTs.put("idType", determineIdType(classe, true));

                if (classMapForTs.containsKey("fields") && classMapForTs.get("fields") instanceof List) {
                    classMapForTs.put("fields", transformFieldsForTypeScript((List<Map<String, Object>>) classMapForTs.get("fields")));
                }

                context.put("class", classMapForTs);
                context.put("projectName", projectName);
                context.put("packageName", packageTarget);
                tsTemplate.merge(context, writer);
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(writer.toString());
                }
            }
        }
    }

    private String determineIdType(JSONObject classe, boolean forTypeScript) {
        String determinedIdType = "Long";

        if (classe.has("idType")) {
            determinedIdType = classe.getString("idType");
        } else if (classe.has("fields")) {
            JSONArray fields = classe.getJSONArray("fields");
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                if (field.optBoolean("isIdField", false) || field.optBoolean("isId", false) || "id".equalsIgnoreCase(field.optString("name"))) {
                    if (field.has("type")) {
                        determinedIdType = field.getString("type");
                        break;
                    }
                }
            }
        }
        return forTypeScript ? dataTypeTsEquals(determinedIdType) : determinedIdType;
    }

    private List<Map<String, Object>> transformFieldsForTypeScript(List<Map<String, Object>> fields) {
        List<Map<String, Object>> transformedFields = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            Map<String, Object> newField = new HashMap<>(field);
            if (newField.containsKey("type") && newField.get("type") instanceof String) {
                newField.put("type", dataTypeTsEquals((String) newField.get("type")));
            }
            transformedFields.add(newField);
        }
        return transformedFields;
    }

    public String dataTypeTsEquals(String type) {
        if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer") ||
                type.equalsIgnoreCase("long") || type.equalsIgnoreCase("Long") ||
                type.equalsIgnoreCase("double") || type.equalsIgnoreCase("Double") ||
                type.equalsIgnoreCase("float") || type.equalsIgnoreCase("Float") ||
                type.equalsIgnoreCase("byte") || type.equalsIgnoreCase("Byte") ||
                type.equalsIgnoreCase("short") || type.equalsIgnoreCase("Short")) {
            return "number";
        } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")) {
            return "boolean";
        } else if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("Character") || type.equalsIgnoreCase("String")) {
            return "string";
        } else if (type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("LocalDate") || type.equalsIgnoreCase("LocalDateTime")) {
            return "string";
        } else {
            return "any";
        }
    }

    private void moveGeneratedFiles(String sourceDir, String targetDir) {
        Path sourcePath = Paths.get(sourceDir);
        Path targetPath = Paths.get(targetDir);

        if (!Files.exists(sourcePath)) {
            System.out.println("Directorio fuente no existe para mover: " + sourceDir);
            return;
        }

        try {
            Files.walk(sourcePath)
                    .forEach(currentSourcePath -> {
                        try {
                            Path relativeSourcePath = sourcePath.relativize(currentSourcePath);
                            Path currentTargetPath = targetPath.resolve(relativeSourcePath);

                            if (Files.isDirectory(currentSourcePath)) {
                                Files.createDirectories(currentTargetPath);
                            } else {
                                Path parentOfTargetFile = currentTargetPath.getParent();
                                if (parentOfTargetFile != null) {
                                    Files.createDirectories(parentOfTargetFile);
                                }
                                Files.copy(currentSourcePath, currentTargetPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            System.err.println("Error al mover el archivo/directorio: " + currentSourcePath + " a " + e.getMessage());
                        }
                    });
            System.out.println("Archivos movidos de " + sourceDir + " a " + targetDir);
        } catch (IOException e) {
            System.err.println("Error al recorrer el directorio fuente: " + e.getMessage());
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            try {
                Files.walk(directory.toPath())
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                System.out.println("Directorio temporal eliminado: " + directory.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error al eliminar el directorio temporal: " + directory.getAbsolutePath() + " - " + e.getMessage());
            }
        }
    }
}