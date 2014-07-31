/*
 *  Copyleft (c) 2014 InterCoding Project
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *  Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 *  URL: https://github.com/marcoapintoo
 *  License: LGPL
 */

package org.pinto.intercoding

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.internal.Lists
import groovy.util.logging.Log4j2
import org.pinto.intercoding.adapter.AdapterModel
import org.pinto.intercoding.adapter.JavaAdapter
import org.pinto.intercoding.format.FormatterModel
import org.pinto.intercoding.format.PythonFormat
import org.pinto.intercoding.intermediate.PythonCodeProcessing
import org.pinto.intercoding.intermediate.NamespaceModel
import org.pinto.intercoding.intermediate.TypeFieldNameResolution

import java.text.SimpleDateFormat

@Log4j2
class InterCoding {
    static final String InterCodingHeader = "InterCoding 1.0 | Java -> Python Translator..."
    AdapterModel adapter
    NamespaceModel currentNamespace

    private void logging(String message = "") {
        print message
        println new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())
    }

    def processObject(String path) {
        try {
            logging("Starting adding object '$path' ... ")
            currentNamespace.copyInto(_read(path))
        } catch (Exception e) {
            exit(e, "ERROR: Joining object has failed!\nAre you sure them are compiled for this version?")
        }
    }

    def processPath(String path) {
        try {
            logging("Starting processing '$path' ... ")
            adapter.process(path)
            currentNamespace = adapter.coreNamespace
        } catch (Exception e) {
            exit(e, "ERROR: Processing paths has failed!")
        }
    }

    private void exit(Exception e, String message) {
        Writer result = new StringWriter()
        e.printStackTrace(new PrintWriter(result))
        log.error result.toString()
        println message
        System.exit(-1)
    }

    def syntacticProcessing() {
        try {
            logging("Starting adaptions on syntax ... ")
            def postProcessing = new PythonCodeProcessing()
            postProcessing.process(currentNamespace)
        } catch (Exception e) {
            exit(e, "ERROR: Syntactic processing has failed!")
        }
    }

    def semanticProcessing() {
        try {
            logging("Starting semantic adaptions on code ... ")
            def nameResolution = new TypeFieldNameResolution(coreNamespace: currentNamespace)
            nameResolution.process(currentNamespace)
        } catch (Exception e) {
            exit(e, "ERROR: Semantic processing has failed!")
        }
    }

    def format(FormatterModel formatter) {
        try {
            logging("Starting formatting with ${formatter.class.name} ... ")
            formatter.coreNamespace = currentNamespace
            formatter.format()
        } catch (Exception e) {
            exit(e, "ERROR: Formatting code has failed!")
        }
    }

    void save(String path) {
        try {
            _save(currentNamespace, path)
        } catch (Exception e) {
            exit(e, "ERROR: Storing code object has failed!")
        }
    }

    private void _save(NamespaceModel namespace, String path) {
        def stream = new ObjectOutputStream(new FileOutputStream(path))
        try {
            //stream.writeObject(adapter.coreNamespace)
            stream.writeObject(namespace)
        } finally {
            stream.close()
        }
    }

    void read(String path) {
        try {
            currentNamespace = _read(path)
        } catch (Exception e) {
            exit(e, "ERROR: Reading code object has failed!")
        }
    }

    private NamespaceModel _read(String path) {
        def stream = new ObjectInputStream(new FileInputStream(path))
        try {
            return stream.readObject() as NamespaceModel
        } finally {
            stream.close()
        }
    }
}

public class ConsoleArguments {
    @Parameter(description = "Files included")
    public List<String> paths = Lists.newArrayList();

    @Parameter(names = ["--include-object", "-i"], description = "Precompiled objects included")
    public List<String> pathObjects = Lists.newArrayList();

    @Parameter(names = ["--log", "--verbose"], description = "Level of verbosity")
    public Integer verbose = 1;

    @Parameter(names = ["--adapter", "-a"], description = "Adapter type. Possible values: [java]")
    public String adapter = "java";

    @Parameter(names = ["--java", "-j"], description = "Use Java adapter (Default)", arity = 1)
    public boolean javaAdapter = true

    @Parameter(names = ["--formatter", "-f"], description = "Output formatter type. Possible values: [python]")
    public String formatter = "python";

    @Parameter(names = ["--python", "-p"], description = "Use Python formatter (Default)", arity = 1)
    public boolean pythonFormatter = true;

    @Parameter(names = ["--output-directory", "-d"], description = "Output directory")
    public String outputDirectory = ".";

    @Parameter(names = ["--output-file", "-o"], description = "Output directory")
    public String outputFileName = "object.db";

    @Parameter(names = ["--help", "-h"], description = "Show this help",  help = true)
    public boolean help;

    /*@DynamicParameter(names = "-D", description = "Dynamic parameters go here")
    public Map<String, String> dynamicParams = new HashMap<String, String>();*/

}

class ConsoleInputInterface {
    private ConsoleArguments consoleArguments
    private JCommander commander

    ConsoleInputInterface() {
        consoleArguments = new ConsoleArguments()
        commander = new JCommander(consoleArguments)
    }

    def run(String[] arguments) {
        showHeader()
        prepare(arguments)
        executeCommands()
    }

    private void showHeader(){
        println InterCoding.InterCodingHeader
    }

    private def prepare(String[] arguments) {
        if (arguments.length == 0) {
            //arguments = ["."].toArray()
            arguments = ["/home/marco/Projects/InterCoding/tests/Test05.java"].toArray()
        }
        commander.parse(arguments)
        if (consoleArguments.javaAdapter) {
            consoleArguments.adapter = "java"
        }
        consoleArguments.adapter = consoleArguments.adapter.trim().toLowerCase()
        if (consoleArguments.pythonFormatter) {
            consoleArguments.formatter = "python"
        }
        consoleArguments.formatter = consoleArguments.formatter.trim().toLowerCase()
    }

    private def executeCommands() {
        if (consoleArguments.help) {
            commander.usage()
            return
        }
        FormatterModel formatter = selectFormatter()
        AdapterModel adapter = selectAdapter()
        new File(consoleArguments.outputDirectory).mkdirs()
        def converter = new InterCoding()
        converter.adapter = adapter
        for (path in consoleArguments.paths) {
            converter.processPath(path)
        }
        for (path in consoleArguments.pathObjects) {
            converter.processObject(path)
        }
        //converter.save(jct.outputDirectory+"/classpath_raw.db")
        converter.syntacticProcessing()
        //converter.save(jct.outputDirectory+"/classpath_syntactic.db")
        converter.semanticProcessing()
        converter.save(consoleArguments.outputDirectory + "/" + consoleArguments.outputFileName)
        converter.format(formatter)
    }

    private FormatterModel selectFormatter() {
        if (consoleArguments.formatter == "python") {
            return new PythonFormat(
                    rootDirectory: consoleArguments.outputDirectory,
                    commonMessage: """
 Copyright (c) 2014 Intercoding Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 URL: https://github.com/marcoapintoo
 License: LGPL
"""
            )
        }
        println consoleArguments.formatter + " is not available as formatter."
        System.exit(-1)
        return null
    }

    private AdapterModel selectAdapter() {
        if (consoleArguments.adapter == "java") {
            return new JavaAdapter()
        }
        println consoleArguments.adapter + " is not available as adapter."
        System.exit(-1)
        return null
    }
}

def consoleInterface = new ConsoleInputInterface()
consoleInterface.run(args)
