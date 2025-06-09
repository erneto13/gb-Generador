package util;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ClassGenerator {

    // Directorios de rutas
    private static final String OUTPUT_DIR = "output";
    private static final String JAVA_FILES_DIR = "JavaFiles";
    private static final String TS_FILES_DIR = "TsFiles";
    private static final String TEMPLATES_DIR = "src/main/resources/templates/";

    // Mapeo de plantillas Java a sus paquetes de destino
    private static final Map<String, String> JAVA_TEMPLATES_TO_PACKAGES = Map.of(
            "ClassTemplate.vm", "models",
            "ControllerTemplate.vm", "controllers",
            "RepositoryTemplate.vm", "repositories",
            "ServiceTemplate.vm", "services"
    );

    // Mapeo de plantillas TypeScript a sus paquetes de destino
    private static final Map<String, String> TS_TEMPLATES_TO_PACKAGES = Map.of(
            "TsModelTemplate.vm", "interfaces",
            "TsServiceTemplate.vm", "services"
    );

    // Mapeo de sufijos para nombres de archivo Java
    private static final Map<String, String> SUFFIX_MAP = Map.of(
            "controllers", "Controller",
            "repositories", "Repository",
            "services", "Service"
    );

    private final JSONArray jsonArray;

    public ClassGenerator(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    /**
     * Método principal modificado para leer la nueva estructura JSON.
     */
    public void generate() {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject projectObject = jsonArray.getJSONObject(i);
            String projectName = projectObject.getString("projectName");

            // 1. Crear la estructura de paquetes base
            generatePackage(JAVA_FILES_DIR + "/controllers", projectName);
            generatePackage(JAVA_FILES_DIR + "/repositories", projectName);
            generatePackage(JAVA_FILES_DIR + "/services", projectName);
            generatePackage(JAVA_FILES_DIR + "/models", projectName);
            generatePackage(TS_FILES_DIR + "/interfaces", projectName);
            generatePackage(TS_FILES_DIR + "/services", projectName);

            // 2. Procesar la lista de paquetes personalizados, si existe
            if (projectObject.has("package")) {
                JSONArray packages = projectObject.getJSONArray("package");
                for (int j = 0; j < packages.length(); j++) {
                    JSONObject packageObj = packages.getJSONObject(j);
                    String packageName = packageObj.getString("packageName");

                    // Crear el directorio para el paquete personalizado
                    generatePackage(JAVA_FILES_DIR + "/" + packageName, projectName);

                    JSONArray classes = packageObj.getJSONArray("classes");
                    for (int k = 0; k < classes.length(); k++) {
                        generateClass(classes.getJSONObject(k), packageName, projectName);
                    }
                }
            }

            // 3. Procesar la clase individual, si existe
            if (projectObject.has("class")) {
                JSONObject classObj = projectObject.getJSONObject("class");
                // Las clases sueltas se generan en el paquete 'models' por defecto
                generateClass(classObj, "models", projectName);
            }
        }
    }

    public void generatePackage(String packageName, String projectName) {
        File packageDir = new File(OUTPUT_DIR + "/" + projectName + "/" + packageName);
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
    }

    public void generateClass(JSONObject classe, String packageName, String projectName) {
        try {
            // Configurar VelocityEngine una sola vez
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
            velocityEngine.setProperty("file.resource.loader.path", TEMPLATES_DIR);
            velocityEngine.init();

            // Generar archivos Java
            generateJavaFiles(velocityEngine, classe, packageName, projectName);

            // Generar archivos TypeScript
            generateTsFiles(velocityEngine, classe, projectName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateJavaFiles(VelocityEngine velocityEngine, JSONObject classe, String packageName, String projectName) throws Exception {
        String baseJavaPath = OUTPUT_DIR + "/" + projectName + "/" + JAVA_FILES_DIR + "/";
        String className = classe.getString("className");

        for (Map.Entry<String, String> entry : JAVA_TEMPLATES_TO_PACKAGES.entrySet()) {
            String templateName = entry.getKey();
            String packageTarget = entry.getValue();

            Template jTemplate = velocityEngine.getTemplate(templateName);
            Context context = new org.apache.velocity.VelocityContext();
            StringWriter writer = new StringWriter();

            String fileName = className;
            if (!templateName.equals("ClassTemplate.vm")) {
                String suffix = SUFFIX_MAP.getOrDefault(packageTarget, "");
                fileName += suffix;
            }

            File file = new File(baseJavaPath + packageTarget + "/" + fileName + ".java");

            // Solo lo crea si el archivo no existe
            if (!file.exists()) {
                file.createNewFile();

                Map<String, Object> classMap = new HashMap<>(classe.toMap());
                classMap.put("idType", determineIdType(classe, false)); // falso para tipos Java

                context.put("class", classMap);
                context.put("projectName", projectName);
                context.put("packageName", packageName);
                context.put("packageTarget", packageTarget);

                jTemplate.merge(context, writer);
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(writer.toString());
                }
            }
        }
    }

    private void generateTsFiles(VelocityEngine velocityEngine, JSONObject classe, String projectName) throws Exception {
        String baseTsPath = OUTPUT_DIR + "/" + projectName + "/" + TS_FILES_DIR + "/";
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

            File file = new File(baseTsPath + packageTarget + "/" + tsFileName + ".ts");

            // Solo lo crea si el archivo no existe
            if (!file.exists()) {
                file.createNewFile();

                Map<String, Object> classMapForTs = new HashMap<>(classe.toMap());
                classMapForTs.put("idType", determineIdType(classe, true)); // true para tipos TypeScript

                // Transformar los tipos de datos de los campos para TypeScript
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

    /**
     * Determina el tipo de ID a partir del objeto JSON de la clase,
     * transformándolo si es necesario para TypeScript.
     *
     * @param classe        The JSONObject representing the class.
     * @param forTypeScript True if the type should be transformed for TypeScript, false for Java.
     * @return The determined ID type string.
     */
    private String determineIdType(JSONObject classe, boolean forTypeScript) {
        String determinedIdType = "Long"; // Default para Java

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

    /**
     * Transforma los tipos de datos de los campos para TypeScript.
     *
     * @param fields La lista de campos a transformar.
     * @return La nueva lista con los tipos de datos transformados.
     */
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

    /**
     * Converts Java data types to their TypeScript equivalents.
     */
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
}