ccrypt-j-cli
========

Command line interface for the ccrypt-j Java library for encrypting and decrypting files (https://github.com/chrsoo/ccrypt-j/) 
compatible with the [ccrypt](http://ccrypt.sourceforge.net/) command line tool. 


## Usage

### Prerquisites

- JRE/JDK >= 11 must be installed (https://adoptopenjdk.net/)

<code>
<pre>
 java -jar ccrypt-j-cli [-d &lt;arg&gt; | -e &lt;arg&gt; | -h]  [-f]
 -d,--decrypt   &lt;arg&gt;   resource to decrypt (file or http)
 -e,--encrypt   &lt;arg&gt;   resource to encrypt (file)
 -f,--force     overwrite existing file
 -h,--help      print the help
</pre>
</code>

## Building from source

Prerquisites
- JRE/JDK 11 or later must be installed (https://adoptopenjdk.net/)
- Maven 3 must be installed

Steps
- Clone the Github project:
```
git clone https://github.com/taulinger/ccrypt-j-cli
```
- Change to the git repository and run Maven:
```
mvn install
```
- compiled artefact `ccrypt-j-cli.jar` can be found under `./target`
