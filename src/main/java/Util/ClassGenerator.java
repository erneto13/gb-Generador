package Util;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeSingleton;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

public class ClassGenerator {
    private JSONArray jsonArray;
    public ClassGenerator(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }
    public void generate(){

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String type = object.getString("type");
            String projectName = object.getString("projectName");
            generatePackage("Controllers", projectName);
            generatePackage("Repositories", projectName);
            generatePackage("Standalone", projectName);
            if(type.equals("Package")){
                String packageName = object.getString("name");
                generatePackage(packageName, projectName);
                JSONArray classes = object.getJSONArray("classes");
                for(int j = 0; j < classes.length(); j++){
                    generateClass(classes.getJSONObject(j), packageName,projectName);
                }
            }else if(type.equals("Class")){
                generateClass(object, "Standalone",projectName);
            }
        }
    }
    public void generatePackage(String packageName, String projectName){
        File packageDir = new File("output/" + projectName + "/" + packageName);
        if (!packageDir.exists()) packageDir.mkdirs();
    }
    public void generateClass(JSONObject classe, String packageName, String projectName) {
        String classPath = "output/" + projectName + "/" + packageName + "/" + classe.getString("className") + ".java";
        try {
            File classFile = new File(classPath);
            if (!classFile.exists()) {
                classFile.createNewFile();
            }
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            Template template = velocityEngine.getTemplate("src/main/resources/Templates/ClassTemplate.vm");
            Context context = new org.apache.velocity.VelocityContext();

            // Crear un objeto "class" para el contexto
            context.put("class", new HashMap() {{
                put("className", classe.getString("className"));
                put("fields", classe.getJSONArray("fields").toList());
                put("isEntity", classe.getBoolean("isEntity"));
            }});
            context.put("projectName", projectName);
            context.put("packageName", packageName);

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            String output = writer.toString();

            FileWriter fileWriter = new FileWriter(classFile);
            fileWriter.write(output);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getClass(String type){
        if(type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer")){
            return "int";
        }
        else if(type.equalsIgnoreCase("long") || type.equalsIgnoreCase("Long")){
            return "long";
        }
        else if(type.equalsIgnoreCase("double") || type.equalsIgnoreCase("Double")){
            return "double";
        }
        else if(type.equalsIgnoreCase("float") || type.equalsIgnoreCase("Float")){
            return "float";
        }
        else if(type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")){
            return "boolean";
        }
        else if(type.equalsIgnoreCase("char") || type.equalsIgnoreCase("Character")){
            return "char";
        }
        else if(type.equalsIgnoreCase("byte") || type.equalsIgnoreCase("Byte")){
            return "byte";
        }
        else if(type.equalsIgnoreCase("short") || type.equalsIgnoreCase("Short")){
            return "short";
        }
        else{
            return "Object";
        }
    }
}
