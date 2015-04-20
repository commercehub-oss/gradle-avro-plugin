package com.commercehub.gradle.plugin.avro;

import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.specs.NotSpec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.commercehub.gradle.plugin.avro.Constants.*;

public class GenerateAvroProtocolTask extends OutputDirTask {
    private static Set<String> SUPPORTED_EXTENSIONS = SetBuilder.build(IDL_EXTENSION, ZIP_EXTENSION, JAR_EXTENSION);

    @TaskAction
    protected void process() {
        getLogger().info("Found {} files", filterSources(new FileExtensionSpec(SUPPORTED_EXTENSIONS)).getFiles().size());
        processFiles();
    }

    private void processFiles() {
        int processedFileCount = 0;
        ClassLoader loader = getRuntimeClassLoader(getProject());
        for (File sourceFile : filterSources(new FileExtensionSpec(IDL_EXTENSION))) {
            processIDLFile(sourceFile, loader);
            processedFileCount++;
        }
        setDidWork(processedFileCount > 0);
    }

    private void processIDLFile(File idlFile, ClassLoader loader) {
        getLogger().info("Processing {}", idlFile);
        File protoFile = new File(getOutputDir(),
                FilenameUtils.getBaseName(idlFile.getName()) + "." + PROTOCOL_EXTENSION);
        try (Idl idl = new Idl(idlFile, loader)) {
            String protoJson = idl.CompilationUnit().toString(true);
            FileUtils.writeStringToFile(protoFile, protoJson, Constants.UTF8_ENCONDING);
        } catch (IOException | ParseException ex) {
            throw new GradleException(String.format("Failed to compile IDL file %s", idlFile), ex);
        }
    }

    private ClassLoader getRuntimeClassLoader(Project project) {
        List<URL> urls = new LinkedList<>();
        Configuration configuration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME);
        for (File file : configuration) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                getLogger().debug(e.getMessage());
            }
        }
        return urls.isEmpty() ? ClassLoader.getSystemClassLoader() :
                new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
    }
}
