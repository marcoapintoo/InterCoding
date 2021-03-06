# InterCoding

## What is this?

InterCoding is a command line tool for translating java to python code.

Java is an awesome language, and relatively simple to parse.
I think there are many interesting libraries, algorithms and other pieces of code
which we can use in other languages without limiting us to the JVM.

## Basic Usage

Use arguments to convert a set of java files:
 
```
java -jar InterCoding.jar "org/uno/Car.java" "org/uno/CarModel.java"
```

Also, we can use directory as arguments:
 
```
java -jar InterCoding.jar "org/uno/"
```

We can select the output directory with "-d" parameter. 

```
java -jar InterCoding.jar -d "python.output" "tests/"
```

## A motivation

Using this input code file:

```java
// File: org/uno/Car.java
package org.uno;

/**
 * Foo class model of a car
 */
class Car{
    public static long serie = 1;
    private long uniqueID;
    private String model;
    /**
     * Base constructor
     */
    public Car(){
        uniqueID = serie;
        String model = "<<base>>";
        serie++;
        this.model = model;
    }
    /**
     * Car's unique ID
     */
    public long getUniqueID(){
        return uniqueID;
    }
    /**
     * Car's model
     */
    public String getModel(){
        return model;
    }
    /**
     * Car's model setter
     */
    public void setModel(String model){
        this.model = model;
    }
} 
```

I obtain this python equivalent:

```python
# File: org.uno.__init__.py
from pinto import *
"""
Foo class model of a car
"""

from java.lang import *
import java.lang
class Car(java.lang.Object):
	"""
	 Auto generated constructor
	"""
	@classmethod
	@multimethod()
	def __class_init__(cls):
		Car.serie = 1
	
	serie = None
	
	uniqueID = None
	
	model = None
	
	"""
	Base constructor
	"""
	@multimethod()
	def __init__(self):
		self.uniqueID = Car.serie
		model = "<<base>>"
		Car.serie += 1
		self.model = model
	
	"""
	Car's unique ID
	"""
	@multimethod()
	def getUniqueID(self):
		return self.uniqueID
	
	"""
	Car's model
	"""
	@multimethod()
	def getModel(self):
		return self.model
	
	"""
	Car's model setter
	"""
	@multimethod(model = String)
	def setModel(self, model):
		self.model = model

Car.__class_init__()
```

## Compiling

Compiling and improving InterConding is really easy. It is a Intellij IDEA project.

Just download, copy, and open.

## Goals

I hope I could create a translated code of almost all JDK's classes using GNU classpath source.

I do not think python is the final target... my original objective was Nimrod,
but it was more easy translate to Python.

I'm on the road!


