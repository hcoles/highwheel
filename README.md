# Highwheel

Highwheel is a tool to detect and visualise class and package cycles in Java code.

It differs from other tools such as

* [JDepend](http://clarkware.com/software/JDepend.html)
* [Classycle](http://http://classycle.sourceforge.net/)

In that it generates visualisations of the detected cycles and provides much more data about the detected dependencies.

To make it easier to make headway when working with legacy code, Highwheel also breaks down large cycles into their
smaller elemental sub-cycles for easier comprehension.

Cycles are detected using [tarjen's algorithm](http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm).

# Documentation

*TODO*

But briefly

## Maven


Analyse a single project, or a each module within a multi-module project individually.

```bash
mvn org.pitest:highwheel-maven:analyse 
```

Analyse a multi-module project as a single component

```bash
mvn -DparentOnly=true org.pitest:highwheel-maven:analyse 
```

By default assumes that your package names match your group id. If not
can supply a filter using Glob syntax.

```bash
mvn -DclassFilter=com.bigcompany.* org.pitest:highwheel-maven:analyse 
```

All options can of course be configured in your pom.xml.

Seperate goal dumps decompiled to disk

mvn org.pitest:highwheel-bytecode:asmDump

## Ant

Create a task and pass in an analysisPath and filter

```xml
<taskdef name="highwheel" classname="org.pitest.highwheel.ant.AnalyseTask" classpathref="<path to jar>"/>

<target name="highwheel" depends="compile">
  <highwheel analysisPathRef="code.path" filter="com.example.*"/>
</target>
```


