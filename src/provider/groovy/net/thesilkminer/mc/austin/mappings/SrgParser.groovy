package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class SrgParser implements Closeable {

    final Reader reader

    // obf class name to map of obf -> srg names
    final Map<String, Map<String, String>> methods = new HashMap<>()
    final Map<String, Map<String, String>> fields = new HashMap<>()
    Map<String, String> workingMethods
    Map<String, String> workingFields

    private final static Pattern PATTERN = ~/(.*) /

    SrgParser(Reader reader) {
        this.reader = reader
        reader.readLine() // Drop header
    }

    @Override
    void close() throws IOException {
        reader.close()
    }

    private void parseLine(String line) {
        var found = (line =~ PATTERN).findAll()
        String obf = found[0]
        obf = obf.substring(0,obf.length()-1)
        String srg = found[-1]
        srg = srg.substring(0,obf.length()-1)
        if (!line.startsWith("\t")) {
            workingFields = new HashMap<>()
            fields.put(obf, workingFields)
            workingMethods = new HashMap<>()
            methods.put(obf, workingMethods)
        } else if (!line.startsWith("\t\t")) {
            if (found.size()==3) {
                workingMethods.put(obf, srg)
            } else {
                workingFields.put(obf,srg)
            }
        }
    }

    parse() {
        for (String line : reader) {
            parseLine(line)
        }
    }
}
