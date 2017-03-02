CSV-X
=====

CSV-X is a schema language, model, and processing engine for non-uniform CSV enabling annotation, validation, cross-referencing, Linked Data, RDF serialization, and transformation to other formats.

Motivation
----------

Current CSV parser or schema cannot deal with non-uniform CSV, the variations of any CSV deviated from RFC4180 "memo".

Data encoded in CSV usually are in table-like structure, i.e. each row represents a record of data while each column fixes the definition of field (meaning, datatype, relation to other fields, etc.) througout the file. But it doesn't have to be!

Thinking of a CSV as a spreadsheet, a cell may has its unique relations with other cells, truly unleashing its expressivity as a tabular-based format!

One of the merit non-uniform CSV schema could bring is to make CSV a compact file format for data transfer while preserving original data structure/model. 

Imagine data in JSON or XML, though its hierarchical structure may help it implicitly define relation between values, but it has to repeat the same structure for every record! (What a waste!)

In contrast, the data structure only need to be described once in CSV-X schema, together with description of encoded CSV value pattern, data can be transferred with less overhead, not yet mentioning other features like datatype validation, annotation, value cross-referencing, RDF serialization, template-based transformation, and Linked Data support. All has made CSV become a Uber-CSV, by using CSV-X. 

Features
--------

- Parsing (Of course!) CSV, TSV, and SSV (Space-Separated Value)
- Annotation, IRI support, thus Linked Data enabled
- Validation (XML Schema Datatype + RegEx)
- Value Alteration
- Cross-Referencing (Dynamic Variable Declaration & Ref, RegEx capturing group)
- RDF Serialization (Turtle)
- Template-based Transformation
- Commandline Interface (CLI)
- New! in v0.11.0 Embedded Scripting (JavaScript)

Requirements
---------------------------

- JRE for CLI
- JDK 8 and Maven for build

Installation
------------

Clone or download the repository, and check out /dist for jar or pre-compile binary 'csvx'.

```
git clone https://github.com/nabito/csv-x
```

or 

Use in code via Maven: (Coming Soon)

```xml
<dependency>
  <groupId>com.dadfha.csvx</groupId>
  <artifactId>csvx</artifactId>
  <version>0.9.6</version>
</dependency>
```

Usage
-----

There are mainly 3 ways you can use CSV-X:

- Try our online demo/playground at <http://dadfha.com/csv-x/>

- Use it as a library in your project. [Javadoc](http://dadfha.com/csv-x/javadoc)

...The package is an eclipse project.

- Commandline (currently only support Mac OS X, Linux, requierd JRE) 

Ex: 
```
csvx -h 
csvx validate input.csv schema.csvx
```

Publication
-----------

IEEE Conference Proceedings published by IEEE CS Press (indexed by EI) with title:
```
CSV-X: A Linked Data Enabled Schema Language, Model, and Processing Engine for Non-Uniform CSV
```
DOI 10.1109/iThings-GreenCom-CPSCom-SmartData.2016.167

Remark
------

This software is a prototype implementation in my research, in other word, it's not in a production quality stage, yet. Any helps in bugs report or, better, bug fixes, feature contribution are more than welcome.

License
-------

[CRAPL](http://matt.might.net/articles/crapl/)--the Community Research and Academic Programming License. 



