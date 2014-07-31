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
class CodeConversion {
    AdapterModel adapter
    NamespaceModel currentNamespace

    private void logging() {
        println new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())
    }

    def processObject(String path) {
        logging()
        currentNamespace.copyInto(_read(path))
    }

    def processPath(String path) {
        logging()
        adapter.process(path)
        currentNamespace = adapter.coreNamespace
    }

    def syntacticProcessing() {
        logging()
        def postProcessing = new PythonCodeProcessing()
        postProcessing.process(currentNamespace)
    }

    def semanticProcessing() {
        logging()
        def nameResolution = new TypeFieldNameResolution(coreNamespace: currentNamespace)
        nameResolution.process(currentNamespace)
    }

    def format(FormatterModel formatter) {
        logging()
        formatter.coreNamespace = currentNamespace
        formatter.format()
    }

    void save(String path) {
        _save(currentNamespace, path)
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
        currentNamespace = _read(path)
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
        prepare(arguments)
        executeCommands()
    }

    private def prepare(String[] arguments) {
        if (arguments.length == 0) {
            arguments = ["."].toArray()
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
        def converter = new CodeConversion()
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
