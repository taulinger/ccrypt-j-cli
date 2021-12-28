ccrypt-j-cli
========

[![Build Status](https://github.com/taulinger/ccrypt-j-cli/workflows/Java%20CI/badge.svg)](https://github.com/taulinger/ccrypt-j-cli/actions/workflows/java.yml?query=branch%3Amain)

Command line interface for the ccrypt-j Java library for encrypting and decrypting files (https://github.com/chrsoo/ccrypt-j/) 
compatible with the [ccrypt](http://ccrypt.sourceforge.net/) command line tool. 


## Usage

Prerequisites

- JRE/JDK 11 or later must be installed (https://adoptium.net/)

<pre>
 java -jar ccrypt-j-cli [-d &lt;arg&gt; | -e &lt;arg&gt; | -h]  [-f]
 -d,--decrypt   &lt;arg&gt;   resource to decrypt (file or http)
 -e,--encrypt   &lt;arg&gt;   resource to encrypt (file)
 -f,--force     overwrite existing file
 -h,--help      print the help
</pre>

## Building from source

Prerequisites
- JDK 11 or later must be installed (https://adoptium.net/)
- Maven 3 must be installed

Steps
- Clone the GitHub project:
```
git clone https://github.com/taulinger/ccrypt-j-cli
```
- Change to the git repository and run Maven:
```
mvn install
```
- compiled artifact `ccrypt-j-cli.jar` can be found under `./target`
