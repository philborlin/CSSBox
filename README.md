[![Build Status](https://travis-ci.org/philborlin/CSSBox.png)](https://travis-ci.org/philborlin/CSSBox)

CSSBox
============

CSSBox is an (X)HTML/CSS rendering engine written in pure Java. Its primary purpose is to provide a complete and further processable
information about the rendered page contents and layout. However, it may be also used for browsing the rendered documents in Java Swing applications.

The input of the rendering engine is the document DOM tree and a set of style sheets referenced from the document.
The output is an object-oriented model of the page layout. This model can be directly displayed but mainly, it is suitable for further processing
by the layout analysis algorithms as for example the page segmentation or information extraction algorithms.

The core CSSBox library may be also used for obtaining a bitmap or vector (SVG) image of the rendered document. Using the SwingBox package,
CSSBox may be used as an interactive web browser component in a Java Swing application.

See the CHANGELOG for the most important changes to the previous versions.

All the source code of CSSBox itself is licensed under the GNU Lesser General
Public License (LGPL), version 3. A copy of the LGPL can be found 
in the LICENSE file.

Building
--------

- CSSBox uses [sbt 0.13](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
to build the project.
	- Use `sbt compile` to compile the project
	- Use `sbt test` to run the tests
	- Use `sbt eclipse` to create an Eclipse project that can be imported using `Import Existing Projects into Workspace`

Required Libraries
------------------
CSSBox relies on the [jStyleParser](https://github.com/philborlin/jStyleParser) open source CSS parser

In the demos contained in the org.fit.cssbox.demo package, the
[NekoHTML parser](http://nekohtml.sourceforge.net/) is used for creating the DOM tree.
As an alternative, the The [Validator.nu HTML Parser](http://about.validator.nu/htmlparser/)
has been tested with CSSBox too.

The Xerces library may be replaced by any other DOM implementation.
