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
package greycat.generator;

import greycat.language.Model;
import greycat.language.ModelChecker;
import java2typescript.SourceTranslator;
import jline.internal.Log;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Generator {
    public static final String FILE_EXTENSION = ".gcm";

    private final Model model;
    private final ModelChecker modelChecker;

    public Generator() {
        this.model = new Model();
        this.modelChecker = new ModelChecker();
    }

    public void scan(File target) throws Exception {
        if (target.isDirectory()) {
            String[] files = target.list();
            if (files == null) {
                throw new RuntimeException("no files to parse found");
            } else {
                for (String name : files) {
                    if (name.trim().endsWith(FILE_EXTENSION)) {
                        this.modelChecker.check(new File(target, name));
                        this.model.parse(new File(target, name));
                    }
                }
            }

        } else if (target.getName().endsWith(FILE_EXTENSION)) {
            this.modelChecker.check(target);
            this.model.parse(target);
        } else {
            throw new RuntimeException("no file with correct extension found");
        }
    }

    public void deepScan(File target) throws Exception {
        if (target.isDirectory()) {
            String[] files = target.list();
            if (files == null) {
                throw new RuntimeException("no files to parse found");
            } else {
                for (String name : files) {
                    if (name.trim().endsWith(FILE_EXTENSION)) {
                        this.modelChecker.check(new File(target, name));
                        this.model.parse(new File(target, name));
                    } else {
                        File current = new File(target, name);
                        if (current.isDirectory()) {
                            deepScan(current);
                        }
                    }
                }
            }

        } else if (target.getName().endsWith(FILE_EXTENSION)) {
            this.modelChecker.check(target);
            this.model.parse(target);
        }
    }

    private void generateJava(String packageName, String pluginName, File target) {
        int index = 0;
        JavaSource[] sources = new JavaSource[(model.classifiers().length) * 2 + 2];
        sources[index] = PluginClassGenerator.generate(packageName, pluginName, model);
        index++;

        JavaSource[] nodeTypes = NodeTypeGenerator.generate(packageName, pluginName, model);
        System.arraycopy(nodeTypes, 0, sources, index, nodeTypes.length);
        index += nodeTypes.length;


        for (int i = 0; i < index; i++) {
            if (sources[i] != null) {
                JavaSource src = sources[i];
                File targetPkg;
                if (src.getPackage() != null) {
                    targetPkg = new File(target.getAbsolutePath() + File.separator + src.getPackage().replace(".", File.separator));
                } else {
                    targetPkg = target;
                }
                targetPkg.mkdirs();
                File targetSrc = new File(targetPkg, src.getName() + ".java");
                try {
                    FileWriter writer = new FileWriter(targetSrc);
                    writer.write(src.toString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateJS(String packageName, String pluginName, File target, String gcVersion, MavenProject mvnProject) {
        // Generate TS


        SourceTranslator transpiler = new SourceTranslator(Arrays.asList(target.getAbsolutePath()), target.getAbsolutePath() + "-ts", packageName);

        if (mvnProject != null) {
            for (Artifact a : mvnProject.getArtifacts()) {
                File file = a.getFile();
                if (file != null) {
                    if (file.isFile()) {
                        transpiler.addToClasspath(file.getAbsolutePath());
                    }
                }
            }
        } else {
            addToTransClassPath(transpiler);
        }

        transpiler.process();
        transpiler.addHeader("import * as greycat from 'greycat'");
        transpiler.generate();

        File tsGen = new File(target.getAbsolutePath() + "-ts" + File.separator + packageName + ".ts");
        try {
            Files.write(tsGen.toPath(), ("export = " + packageName).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tsConfigContent = "{\n" +
                "  \"compilerOptions\": {\n" +
                "    \"module\": \"commonjs\",\n" +
                "    \"noImplicitAny\": false,\n" +
                "    \"removeComments\": true,\n" +
                "    \"preserveConstEnums\": true,\n" +
                "    \"sourceMap\": true,\n" +
                "    \"target\": \"es5\",\n" +
                "    \"declaration\": true,\n" +
                "    \"outDir\": \"lib\"\n" +
                "  },\n" +
                "  \"files\": [\n" +
                "    \"" + packageName + ".ts\"\n" +
                "  ]\n" +
                "}";
        try {
            File tsConfig = new File(target.getAbsolutePath() + "-ts" + File.separator + "tsconfig.json");
            tsConfig.createNewFile();
            Files.write(tsConfig.toPath(), tsConfigContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isSnaphot = (gcVersion.contains("SNAPSHOT"));
        gcVersion = isSnaphot ? "../../../../../greycat/target/classes-npm" : "^" + gcVersion + ".0.0";

        String packageJsonContent = "{\n" +
                "  \"name\": \"" + packageName + "\",\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"description\": \"\",\n" +
                "  \"main\": \"lib/" + packageName + "\",\n" +
                "  \"author\": \"\",\n" +
                "  \"types\": \"lib/" + packageName + "\",\n" +
                "  \"description\":\"empty\",\n" +
                "  \"repository\":\"empty\",\n" +
                "  \"license\":\"UNLICENSED\"," +
                "  \"dependencies\": {\n" +
                "    \"greycat\": \"" + gcVersion + "\"\n" +
                "  },\n" +
                "  \"devDependencies\": {\n" +
                "    \"typescript\": \"^2.1.5\"\n" +
                "  }" +
                "}";
        try {
            File packageJson = new File(target.getAbsolutePath() + "-ts" + File.separator + "package.json");
            packageJson.createNewFile();
            Files.write(packageJson.toPath(), packageJsonContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Generate a base of NPM project
        File npmProject = new File(target.getAbsolutePath() + "-starter");
        npmProject.mkdirs();
        File mainJS = new File(npmProject, "main.js");
        File packageJson2 = new File(npmProject, "package.json");
        File readme = new File(npmProject, "readme.md");
        File mainTS = new File(npmProject, "main2.ts");
        try {
            mainJS.createNewFile();
            Files.write(mainJS.toPath(), ("var greycat = require(\"greycat\");\n" +
                    "var " + packageName + " = require(\"" + packageName + "\");\n" +
                    "\n" +
                    "var g = greycat.GraphBuilder.newBuilder().withPlugin(new " + packageName + "." + pluginName + "()).build();\n" +
                    "\n" +
                    "g.connect(function (isSucceed) {\n" +
                    "console.log(\"--- GreyCat ready ---\");\n" +
                    "    var n = g.newNode(0,0);\n" +
                    "    n.set(\"name\",greycat.Type.STRING, \"myName\");\n" +
                    "    console.log(n.toString());\n" +
                    "});").getBytes());

            packageJson2.createNewFile();
            Files.write(packageJson2.toPath(), ("{\n" +
                    "  \"name\": \"" + packageName + "-starter\",\n" +
                    "  \"version\": \"1.0.0\",\n" +
                    "  \"description\": \"\",\n" +
                    "  \"main\": \"main.js\",\n" +
                    "  \"author\": \"\",\n" +
                    "  \"description\":\"empty\",\n" +
                            "  \"repository\":\"empty\",\n" +
                            "  \"license\":\"UNLICENSED\","+
                    "  \"dependencies\": {\n" +
                    "    \"greycat\": \"" + gcVersion + "\",\n" +
                    "    \"" + packageName + "\": \"../greycat-modeling-ts\"\n" +
                    "  },\n" +
                    "  \"devDependencies\": {\n" +
                    "    \"typescript\": \"^2.1.5\",\n" +
                    "    \"ts-node\": \"^3.0.4\"\n" +
                    "  }" +
                    "}").getBytes());

            Files.write(readme.toPath(), ("# JavaScript usage\n" +
                    "\n" +
                    "`node main.js\n" +
                    "\n" +
                    "# TypeScript usage\n" +
                    "\n" +
                    "*(only the first time)*\n" +
                    "\n" +
                    "`npm install -g ts-node typescript`\n" +
                    "\n" +
                    "then\n" +
                    "\n" +
                    "`ts-node main2.ts`").getBytes());
            Files.write(mainTS.toPath(), ("import * as greycat from 'greycat';\n" +
                    "import * as " + packageName + " from '" + packageName + "';\n" +
                    "\n" +
                    "var g = greycat.GraphBuilder.newBuilder().withPlugin(new " + packageName + ".ModelPlugin()).build();\n" +
                    "\n" +
                    "g.connect(function (isSucceed) {\n" +
                    "    console.log(\"--- GreyCat ready ---\");\n" +
                    "    var n = g.newNode(0,0);\n" +
                    "    n.set(\"name\",greycat.Type.STRING, \"myName\");\n" +
                    "    console.log(n.toString());\n" +
                    "})\n").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        File workingDFir = new File(target.getAbsolutePath() + "-ts");
        // Install required package in TS
        ProcessBuilder processBuilder = new ProcessBuilder("npm", "install");
        processBuilder.directory(workingDFir);
        processBuilder.inheritIO();
        // Run TSC
        ProcessBuilder processBuilder2 = new ProcessBuilder("node", "node_modules/typescript/lib/tsc.js");
        processBuilder2.directory(workingDFir);
        processBuilder2.inheritIO();
        //Install required packaged in JS project
        ProcessBuilder processBuilder3 = new ProcessBuilder("npm", "install");
        processBuilder3.directory(npmProject);
        processBuilder3.inheritIO();
        try {
            processBuilder.start().waitFor();
            processBuilder2.start().waitFor();
            processBuilder3.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mvnGenerate(String packageName, String pluginName, File target, boolean generateJava, boolean generateJS, String gcVersion, MavenProject project) {
        if (generateJava || generateJS) {
            generateJava(packageName, pluginName, target);
        }
        if (generateJS) {
            generateJS(packageName, pluginName, target, gcVersion, project);
        }
    }

    public void generate(String packageName, String pluginName, File target, boolean generateJava, boolean generateJS, String gcVersion) {
        if (generateJava || generateJS) {
            generateJava(packageName, pluginName, target);
        }

        if (generateJS) {
            generateJS(packageName, pluginName, target, gcVersion, null);
        }
    }

    private void addToTransClassPath(SourceTranslator transpiler) {
        String classPath = System.getProperty("java.class.path");
        int index = 0;
        boolean finish = false;
        while (index < classPath.length() && !finish) {
            if (classPath.charAt(index) == ':') {
                int slashIdx = index;
                while (slashIdx >= 0 && !finish) {
                    if (classPath.charAt(slashIdx) == '/') {
                        if (slashIdx + 7 < index && classPath.charAt(slashIdx + 1) == 'g' && classPath.charAt(slashIdx + 2) == 'r' && classPath.charAt(slashIdx + 3) == 'e'
                                && classPath.charAt(slashIdx + 4) == 'y' && classPath.charAt(slashIdx + 5) == 'c' && classPath.charAt(slashIdx + 6) == 'a' && classPath.charAt(slashIdx + 7) == 't') {
                            while (slashIdx >= 0 && !finish) {
                                if (classPath.charAt(slashIdx) == ':') {
                                    transpiler.addToClasspath(classPath.substring(slashIdx + 1, index));
                                    finish = true;
                                }
                                slashIdx--;
                            }
                        }
                    }
                    slashIdx--;
                }
            }
            index++;
        }
    }


}
