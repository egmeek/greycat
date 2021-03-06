/**
 * Copyright 2017 The GreyCat Authors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greycat.mavenplugin;

import greycat.generator.Generator;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GeneratorPlugin extends AbstractMojo {

    /**
     * File or directory containing the model definition.
     * If there are several files in the directory they will be merged into one.
     * <p>
     * File(s) should have the "{@value Generator#FILE_EXTENSION}".
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/resources")
    private File srcFiles;

    /**
     * Defines if a sub directories should be considered or not
     */
    @Parameter(defaultValue = "false", alias = "deepScan")
    private boolean doDeepScan;

    /**
     * GreyCat plugin name
     */
    @Parameter(defaultValue = "ModelPlugin")
    private String pluginName;

    /**
     * Root package in which the Java classes are generated.
     * They are translated into TypeScript namespaces.
     */
    @Parameter(defaultValue = "model")
    private String packageName;

    /**
     * Folder in which the files should be generated
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/greycat-modeling")
    private File targetGen;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Defines if the Java classes are generated
     */
    @Parameter(defaultValue = "true")
    private boolean generateJava;

    /**
     * Defines if the JavaScript classes are generated
     */
    @Parameter(defaultValue = "false")
    private boolean generateJS;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Generator generator = new Generator();
        try {
            if (doDeepScan) {
                generator.deepScan(srcFiles);
            } else {
                generator.scan(srcFiles);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error during file scanning", e);
        }
        String gcVersion = "";
        for (int i = 0; i < project.getDependencies().size(); i++) {
            Dependency dependency = project.getDependencies().get(i);
            if (dependency.getGroupId().equals("com.datathings") && dependency.getArtifactId().equals("greycat")) {
                gcVersion = dependency.getVersion();
                break;
            }
        }
        generator.mvnGenerate(packageName, pluginName, targetGen, generateJava, generateJS, gcVersion, project);
        project.addCompileSourceRoot(targetGen.getAbsolutePath());
    }
}
