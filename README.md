<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/nuget-adapter/blob/master/LICENSE.txt)

This Java library turns your binary [ASTO](https://github.com/artipie/asto) 
storage into a NuGet repository.

Similar solutions:

  * [Artifactory](https://www.jfrog.com/confluence/display/RTF/NuGet+Repositories)
  * [NuGet](https://www.nuget.org/)

Some valuable references:

  * [NuGet Documentation](https://docs.microsoft.com/en-us/nuget/)
  * [NuGet Sources](https://github.com/NuGet)

## Getting started

Add dependency to `pom.xml`:

```xml
<dependency>
  <groupId>com.artipie</groupId>
  <artifactId>nuget-adapter</artifactId>
  <version>[...]</version>
</dependency>
```

Save NuGet package ZIP file like `package.nupkg` (particular name does not matter)
to [ASTO](https://github.com/artipie/asto) storage. 
Then, make an instance of `Repository` class with storage as an argument.
Finally, instruct `Repository` to add the package to repository:

```java
import com.artpie.nuget;
Repository repo = new Repository(storage);
repo.add(new Key.From("package.nupkg"));
```

## Project status

- [x] Adding package to repository [#1](https://github.com/artipie/nuget-adapter/issues/1)
- [ ] HTTP support for installing package [#19](https://github.com/artipie/nuget-adapter/issues/19)
- [ ] HTTP support for adding package [#20](https://github.com/artipie/nuget-adapter/issues/20)
- [ ] HTTP support for listing package versions [#29](https://github.com/artipie/nuget-adapter/issues/29)

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+.
