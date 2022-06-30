package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic

@CompileStatic
class OfficialParser implements Closeable {

    final Reader reader

    // obf class name to map of official -> obf names
    final Map<String, Map<String, String>> methods = new HashMap<>()
    final Map<String, Map<String, String>> fields = new HashMap<>()
    // obf -> official
    final Map<String, String> classes = new HashMap<>()
    Map<String, String> workingMethods
    Map<String, String> workingFields

    OfficialParser(Reader reader) {
        this.reader = reader
        reader.readLine() // Drop header
    }

    @Override
    void close() throws IOException {
        reader.close()
    }

    private void parseLine(String line) {
        var found = line.split(' ').findAll {it.length()!=0}
        if (found[-1].endsWith(':')) {
            String obf = found[-1].substring(0,found[-1].length()-1)
            String official = found[0]
            workingFields = new HashMap<>()
            fields.put(obf, workingFields)
            workingMethods = new HashMap<>()
            methods.put(obf, workingMethods)
            classes.put(obf,official)
        } else {
            String official = found[1]
            String obf = found[-1]
            if (found[0].contains(':')) {
                //method
                workingMethods.put(official, obf)
            } else {
                workingFields.put(official, obf)
            }
        }
    }

    parse() {
        for (String line : reader) {
            parseLine(line)
        }
    }
}
