package org.wearefrank.xsltdebugger;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**A class that is used to be able to use both files and xml/xsl text as input.
 * If */
@AllArgsConstructor
public class XMLTransformationContext {
    private String name;
    private String absolutePath;
    @Getter
    @NotNull
    private String context;

    /**Converts file into an XMLTransformationContext*/
    public static XMLTransformationContext createContextFromFile(File file) {
        try {
            String context = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            return new XMLTransformationContext(file.getAbsolutePath(), file.getName(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public XMLTransformationContext(String context){
        this.context = context;
    }

    //When only text is given without a file, the name is consistent so that a comparison can be made when the name is called.
    public String getName(){
        if(name == null){
            return "XSL input";
        }else{
            return name;
        }
    }

    //When only text is given without a file, the path is consistent so that a comparison can be made when the absolute path is called.
    public String getAbsolutePath() {
        if(absolutePath == null){
            return "XSL input";
        }else{
            return absolutePath;
        }
    }

    //When only text is given without a file, the systemId is consistent so that a comparison can be made when the systemId is called.
    public String getSystemId() {
        if(absolutePath == null){
            return "XSL input";
        }else{
            try {
                URI uri = new URI("file", null, absolutePath, null);
                return uri.toString();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    //Was put here so less duplicated code is in the XSLTReporterSetup class
    public StreamSource getSourceObject(){
        StreamSource source = new StreamSource(new StringReader(this.getContext()));
        source.setSystemId(this.getSystemId());
        return source;
    }
}
