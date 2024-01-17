package org.wearefrank.xsltdebugger;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@AllArgsConstructor
public class XMLTransformationContext {
    private String name;
    private String absolutePath;
    private String context;

    public static XMLTransformationContext createContextFromFile(File file) {
        try {
            String context = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            return new XMLTransformationContext(file.getAbsolutePath(), file.getName(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(){
        if(name == null){
            return "N/A";
        }else{
            return name;
        }
    }

    public String getAbsolutePath() {
        if(absolutePath == null){
            return "N/A";
        }else{
            return absolutePath;
        }
    }

    public String getContext(){
        if(context == null){
            return "N/A";
        }else{
            return context;
        }
    }
}
