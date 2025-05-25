package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

public class ClassGenerator {
    private JSONArray jsonArray;

    public ClassGenerator(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public void generate() {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String type = object.getString("type");
            String projectName = object.getString("projectName");
            generatePackage("JavaFiles/controllers", projectName);
            generatePackage("JavaFiles/repositories", projectName);
            generatePackage("JavaFiles/services", projectName);
            generatePackage("JavaFiles/models", projectName);
            generatePackage("TsFiles/models", projectName);
            generatePackage("TsFiles/service", projectName);
            if (type.equals("Package")) {
                String packageName = object.getString("name");
                generatePackage("JavaFiles/" + packageName, projectName);
                JSONArray classes = object.getJSONArray("classes");
                for (int j = 0; j < classes.length(); j++) {
                    generateClass(classes.getJSONObject(j), packageName, projectName);
                }
            } else if (type.equals("Class")) {
                generateClass(object, "models", projectName);
            }
        }
    }

    public void generatePackage(String packageName, String projectName) {
        File packageDir = new File("output/" + projectName + "/" + packageName);
        if (!packageDir.exists()) packageDir.mkdirs();
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
        tsPackagesToTemplates.put("TsModelTemplate.vm", "models");
        tsPackagesToTemplates.put("TsServiceTemplate.vm", "service");
        try {
            for (String template : javaTemplates) {
                VelocityEngine velocityEngine = new VelocityEngine();
                velocityEngine.init();
                Template jTemplate = velocityEngine.getTemplate("src/main/resources/Templates/" + template);
                Context context = new org.apache.velocity.VelocityContext();
                StringWriter writer = new StringWriter();
                String packageTarget = javaPackagesToTemplates.get(template);
                File file;
                // Si el packageName no está en los valores de javaPackagesToTemplates, usar packageName como destino
                if (!javaPackagesToTemplates.containsValue(packageName)) {
                    // Solo para ClassTemplate, el archivo va en el packageName recibido
                    if (template.equals("ClassTemplate.vm")) {
                        file = new File(baseJavaPath + packageName + "/" + classe.getString("className") + ".java");
                    } else {
                        // Para los otros templates, usar su packageTarget correspondiente
                        file = new File(baseJavaPath + packageTarget + "/" + classe.getString("className") + packageTarget + ".java");
                    }
                } else {
                    // Si el packageName sí está en los valores, usar el packageTarget
                    file = new File(baseJavaPath + packageTarget + "/" + classe.getString("className") + packageTarget + ".java");
                }
                if (!file.exists()) {
                    file.createNewFile();
                    context.put("class", new HashMap() {{
                        put("className", classe.getString("className"));
                        put("fields", classe.getJSONArray("fields").toList());
                        put("isEntity", classe.getBoolean("isEntity"));
                    }});
                    context.put("projectName", projectName);
                    context.put("packageName", packageTarget);
                    jTemplate.merge(context, writer);
                    String output = writer.toString();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(output);
                    fileWriter.close();
                }
            }
            for (String template : tsTemplates){
                VelocityEngine velocityEngine = new VelocityEngine();
                velocityEngine.init();
                Template tsTemplate = velocityEngine.getTemplate("src/main/resources/Templates/" + template);
                Context context = new org.apache.velocity.VelocityContext();
                StringWriter writer = new StringWriter();
                String packageTarget = tsPackagesToTemplates.get(template);
                if(packageTarget != null) {
                    File file = new File(baseTsPath + packageTarget + "/" + classe.getString("className") + ".ts");
                    if (!file.exists()) file.createNewFile();
                    context.put("class", new HashMap() {{
                        put("className", classe.getString("className"));
                        put("fields", classe.getJSONArray("fields").toList());
                        put("isEntity", classe.getBoolean("isEntity"));
                    }});
                    context.put("projectName", projectName);
                    context.put("packageName", packageTarget);
                    tsTemplate.merge(context, writer);
                    String output = writer.toString();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(output);
                    fileWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        } else {
            return "Object";
        }
    }
    public String dataTypeTsEquals(String type){
        if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer")) {
            return "number";
        } else if (type.equalsIgnoreCase("long") || type.equalsIgnoreCase("Long")) {
            return "number";
        } else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("Double")) {
            return "number";
        } else if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("Float")) {
            return "number";
        } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")) {
            return "boolean";
        } else if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("Character")) {
            return "string";
        } else if (type.equalsIgnoreCase("byte") || type.equalsIgnoreCase("Byte")) {
            return "number";
        } else if (type.equalsIgnoreCase("short") || type.equalsIgnoreCase("Short")) {
            return "number";
        } else {
            return "any";
        }
    }
}
