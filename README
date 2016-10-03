# CSV-X
=====================================

CSV-X is a schema language, model, and processing engine for non-uniform CSV enabling annotation, validation, cross-referencing, Linked Data, RDF serialization, and transformation to other formats.

## Motivation
------------------

Current CSV parser or schema cannot deal with non-uniform CSV, the variations of any CSV deviated from RFC4180 "memo".

Data encoded in CSV usually are in table-like structure, i.e. each row represents a record of data while each column fixes the definition of field (meaning, datatype, relation to other fields, etc.) througout the file. But it doesn't have to be!

Thinking of a CSV as a spreadsheet, a cell may has its unique relations with other cells, truly unleashing its expressivity as a tabular-based format!

One of the merit non-uniform CSV schema could bring is to make CSV a compact file format for data transfer while preserving original data structure/model. 

Imagine data in JSON or XML, though its hierarchical structure may help it implicitly define relation between values, but it has to repeat the same structure for every record! (What a waste!)

In contrast, the data structure only need to be described once in CSV-X schema, together with description of encoded CSV value pattern, data can be transferred with less overhead, not yet mentioning other features like datatype validation, annotation, value cross-referencing, RDF serialization, template-based transformation, and Linked Data support. All has made CSV become a Uber-CSV, by using CSV-X. 

## Features
------------------

- Parsing (Of course!) CSV, TSV, and SSV (Space-Separated Value)
- Annotation, IRI support, thus Linked Data enabled
- Validation (XML Schema Datatype + RegEx)
- Cross-Referencing
- RDF Serialization (Turtle)
- Template-based Transformation

## Installation
------------------

Use maven:

```xml
<dependency>
  <groupId>com.dadfha.csvx</groupId>
  <artifactId>csvx</artifactId>
  <version>0.9.6</version>
</dependency>
```

## Usage
------------------

There are mainly 3 ways you can use CSV-X:

- Try our online demo/playground at <http://dadfha.com/csv-x>

- Use it as a library in your project. [Javadoc](http://dadfha.com/csv-x/javadoc)

- Commandline (*nix tested, requierd Java 8)

## History
------------------

Once upon a time, there was a legendary file format, being well-known for its ease of use in data sharing, the CSV. However, after an invent of newly, more powerful and sophisticated format like XML, JSON, and RDF, everyone thought CSV was dead.. but guess what, the facts is, it's still alive and still being used widely in open data community due to its simplicity, compactness, and universally supports by 3rd party applications. 

There's undeniable truth that we've to reconsider CSV from its root and make it great again! (Nah, I'm not "his" fan in anyway, but I can't let he hijacked this phrase). And this is how it begins, CSV-X, a schema language that will change everything since CSV. 

## Published Paper
------------------

TBD

## Remark
------------------

This software is a prototype implementation in my research, in other word, it's not in a production quality stage, yet. Any helps in bugs report or, better, bug fixes, feature contribution are more than welcome.

## License
------------------

[CRAPL](http://matt.might.net/articles/crapl/)--the Community Research and Academic Programming License. 



