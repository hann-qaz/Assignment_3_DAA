.PHONY: build test run benchmark all
SOURCES := $(shell find src tests -name '*.java')
build:
	mkdir -p build
	javac --release 17 -encoding UTF-8 -d build $(SOURCES)
test: build
	java -cp build tests.AlgorithmTests
run: build
	java -cp build app.Main
benchmark: build
	java -cp build benchmark.Benchmark
all: test run benchmark
