# Austin's Powerful Language Provider: Keeping It Groovy Baby

The Grooviest language provider in Forgeland

## How to use
To use this language provider, simply add it as a dependency in your build script. The
language provider is available on Jared's maven, so you can simply add the following
to your build script, replacing `${mcver}` with the Minecraft version you are targeting
and `${version}` with the version of APLP you want to use:
```groovy
repositories {
    maven {
        name 'BlameJared'
        url = 'https://maven.blamejared.com'
    }
}
dependencies {
    compileOnly group: 'net.thesilkminer.mc.austin', name: "aplp-${mcver}", version: "${version}"
    runtimeOnly group: 'net.thesilkminer.mc.austin', name: "aplp-${mcver}", version: "${version}", classifier: 'all'
}
```

Also make sure to add the `groovy` plugin to your build script to ensure that the required
tasks for Groovy compilation are added:
```groovy
plugins {
    id 'groovy'
}
```

In your `mods.toml`, specify `aplp` as the loader and the version of the software
as `loaderVersion`. See the following for a bare-bones example:

```toml
modLoader="aplp"
loaderVersion="[1,)"
license="All Rights Reserved"

[[mods]]
modId="mymojo"
version="1.0.0"
```

The main mod class should have a no-arg constructor and be annotated with either
`@Mojo` (suggested) or `@Mod` with its mod ID installed. Access to the mod and Forge
buses is provided respectively through the `mojoBus` and `forgeBus` properties or
through the `@GrabEventBus` annotation:

```groovy
@Mojo("mymojo")
class MyMojo {
    @GrabEventBus(EventBus.FORGE)
    private final IEventBus grabbedForgeBus
    
    MyMojo() {
        LogManager.getLogger(MyMod).info('Hello from Groovy-land!')
        mojoBus.register(ModBusEventHandler)
        forgeBus.register(ForgeBusEventHandler)
        assert grabbedForgeBus === forgeBus
    }
}
```

### Groovy Version

The Groovy version provided by this language provider is: **4.0.3**.

Only actual releases will be supported by this language provider, not release candidates.
Moreover, once a version of Forge is deemed to be production-ready (i.e. a Recommended
Build is published), both the major and minor components of the Groovy version will be
locked. Patch releases will instead be updated periodically.

### Provided Modules

The modules that are provided by default in this language provider do not cover the
whole set of Groovy modules, but should still suffice for both common and some more
particular use cases.

- **Standard Library**: The essential bits to allow Groovy to work.
- **Ant**: A Groovy interface to Apache Ant, for dependency management.
- **AST Builder**: Captures AST builders from code statements.
- **Contracts**: Allows specification of invariants, pre-, and post-conditions.
- **Date-Time**: Groovy extensions for the Java Date-Time library.
- **Date-Util**: Groovy extensions for the older Java Date library.
- **GINQ**: LINQ-style collection querying interfaces.
- **JSON**: Read and write JSON with a Groovy-like interface.
- **JSR223**: Allows usage of GroovyScript in the Java Scripting Engine.
- **JUnit 5 Test**: Testing framework; useful in general or in scripting scenarios.
- **Macro**: Supports the usage of Groovy Macros.
- **Macro Library**: Additional Macro extensions.
- **NIO**: Extension methods to allow for NIO usage in a Groovy-like fashion.
- **PicoCLI**: Allows for compact CLI interfaces.
- **Templates**: Basic templating usage with a set of engines.
- **Test**: Testing framework; useful in general or in scripting scenarios.
- **TOML**: Read and write TOML with a Groovy-like interface.
- **Type Checkers**: Allows verification of regex.
- **XML**: Read and write XML with a Groovy-like interface.
- **YAML**: Read and write YAML with a Groovy-like interface.

### Dropped Modules

Some modules have been dropped from this language provider due to various reasons. They
are listed in the following text.

- **Binary**: Batch files to call Groovy programs are not required.
- **BOM**: Useful only for development.
- **BSF**: Not available.
- **CLI Commons**: PicoCLI is already available.
- **Compatibility with 2 & 3**: No previous releases with Groovy 2 or Groovy 3 were made.
- **Console**: Interactive execution of Groovy is outside the scope of this LP.
- **Doc Generator**: Useful only for development.
- **GroovyDoc**: Useful only for development.
- **GroovySH**: Interactive execution of Groovy is outside the scope of this LP.
- **JAXB**: Mainly used for Enterprise Development.
- **JMX**: Profiling tools, hardly useful in production.
- **Servlet**: HTML web servers as MC mods?
- **SQL**: Interacting with a SQL database is extremely rare.
- **Swing**: Swing cannot be used in MC without major issues.
- **TestNG**: Not used as much as JUnit.

If you disagree with any of these reasons, please reach out to discuss.
