package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class ClassGenerator {

    // Directorios de rutas
    private static final String OUTPUT_DIR = "output";
    private static final String JAVA_FILES_DIR = "JavaFiles";
    private static final String TS_FILES_DIR = "TsFiles";
    private static final String TEMPLATES_DIR = "src/main/resources/templates/";

    private JSONArray jsonArray;

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
            generatePackage("JavaFiles/controllers", projectName);
            generatePackage("JavaFiles/repositories", projectName);
            generatePackage("JavaFiles/services", projectName);
            generatePackage("JavaFiles/models", projectName);
            generatePackage("TsFiles/interfaces", projectName);
            generatePackage("TsFiles/services", projectName);

            // 2. Procesar la lista de paquetes personalizados, si existe
            if (projectObject.has("package")) {
                JSONArray packages = projectObject.getJSONArray("package");
                for (int j = 0; j < packages.length(); j++) {
                    JSONObject packageObj = packages.getJSONObject(j);
                    String packageName = packageObj.getString("packageName");

                    // Crear el directorio para el paquete personalizado
                    generatePackage("JavaFiles/" + packageName, projectName);

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
        File packageDir = new File("output/" + projectName + "/" + packageName);
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
    }

    public void generateClass(JSONObject classe, String packageName, String projectName) {
        String baseJavaPath = "output/" + projectName + "/" + "JavaFiles/";
        String baseTsPath = "output/" + projectName + "/TsFiles/";
        List<String> javaTemplates = List.of("ClassTemplate.vm", "ControllerTemplate.vm", "RepositoryTemplate.vm", "ServiceTemplate.vm");
        List<String> tsTemplates = List.of("TsModelTemplate.vm", "TsServiceTemplate.vm");
        HashMap<String, String> javaPackagesToTemplates = new HashMap<>();
        javaPackagesToTemplates.put("ClassTemplate.vm", "models");
        javaPackagesToTemplates.put("ControllerTemplate.vm", "controllers");
        javaPackagesToTemplates.put("RepositoryTemplate.vm", "repositories");
        javaPackagesToTemplates.put("ServiceTemplate.vm", "services");
        HashMap<String, String> tsPackagesToTemplates = new HashMap<>();
        tsPackagesToTemplates.put("TsModelTemplate.vm", "interfaces");
        tsPackagesToTemplates.put("TsServiceTemplate.vm", "services");
        try {
            // Generación de archivos Java
            for (String template : javaTemplates) {
                VelocityEngine velocityEngine = new VelocityEngine();
                velocityEngine.init();
                Template jTemplate = velocityEngine.getTemplate(TEMPLATES_DIR + template);
                Context context = new org.apache.velocity.VelocityContext();
                StringWriter writer = new StringWriter();
                String packageTarget = javaPackagesToTemplates.get(template);
                String className = classe.getString("className");
                String fileName = className;
                File file;

                // Determina el nombre del archivo y la ruta de destino
                if (template.equals("ClassTemplate.vm")) {
                    // El archivo de la clase principal va en su paquete correspondiente (ej: models, o uno personalizado)
                    file = new File(baseJavaPath + packageName + "/" + fileName + ".java");
                } else {
                    // Los archivos de Controller, Service, Repository usan sufijos y van a sus paquetes estándar
                    String suffix = packageTarget.substring(0, 1).toUpperCase() + packageTarget.substring(1, packageTarget.length() - 1); // Controller, Service, etc.
                    fileName += suffix;
                    file = new File(baseJavaPath + packageTarget + "/" + fileName + ".java");
                }

                if (!file.exists()) {
                    file.createNewFile();
                    // Añade los datos al contexto de Velocity
                    context.put("class", classe.toMap());
                    context.put("projectName", projectName);
                    context.put("packageName", packageName); // Paquete original de la clase
                    context.put("packageTarget", packageTarget); // Paquete destino del archivo actual

                    jTemplate.merge(context, writer);
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(writer.toString());
                    }
                }
            }

            // Generación de archivos TypeScript
            for (String template : tsTemplates) {
                VelocityEngine velocityEngine = new VelocityEngine();
                velocityEngine.init();
                Template tsTemplate = velocityEngine.getTemplate(TEMPLATES_DIR + template);
                Context context = new org.apache.velocity.VelocityContext();
                StringWriter writer = new StringWriter();
                String packageTarget = tsPackagesToTemplates.get(template);
                if (packageTarget != null) {
                    File file = new File(baseTsPath + packageTarget + "/" + classe.getString("className") + ".ts");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    context.put("class", classe.toMap());
                    context.put("projectName", projectName);
                    context.put("packageName", packageTarget);
                    tsTemplate.merge(context, writer);
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(writer.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Los métodos de ayuda no necesitan cambios
    public String dataTypeJavaEquals(String type) {
        if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer")) {
            return "int";
        } else if (type.equalsIgnoreCase("long") || type.equalsIgnoreCase("Long")) {
            return "long";
        } else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("Double")) {
            return "double";
        } else if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("Float")) {
            return "float";
        } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")) {
            return "boolean";
        } else if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("Character")) {
            return "char";
        } else if (type.equalsIgnoreCase("byte") || type.equalsIgnoreCase("Byte")) {
            return "byte";
        } else if (type.equalsIgnoreCase("short") || type.equalsIgnoreCase("Short")) {
            return "short";
        } else if (type.equalsIgnoreCase("String")) {
            return "String";
        } else {
            return "Object";
        }
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
        } else {
            return "any";
        }
    }
}