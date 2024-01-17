package org.wearefrank.xsltdebugger;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@AllArgsConstructor
public class XMLTransformationContext {
    private String name;
    private String absolutePath;
    @Getter
    @NotNull
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
            return "XSL input";
        }else{
            return name;
        }
    }

    public String getAbsolutePath() {
        if(absolutePath == null){
            return "XSL input";
        }else{
            return absolutePath;
        }
    }
}
