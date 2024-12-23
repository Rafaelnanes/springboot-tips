## Native Image
To create the executable, run the following goal:

```
./gradlew nativeCompile
```

Then, you can run the app as follows:
```
build/native/nativeCompile/graalvm-cds
```

To create a docker image:
```
docker build -t my-app .
```

Image size: 1.04 GB

## Buildpack
To create the image, run the following goal:

```
./gradlew bootBuildImage
```

Then, you can run the app like any other container:
```
docker run --rm -p 8080:8080 graalvm-cds:0.0.1-SNAPSHOT
```

Image size: 351 MB