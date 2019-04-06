package main

import (
	"bufio"
	"flag"
	"github.com/Lepovirta/lukki/config"
	"io"
	"log"
	"os"
)

var (
	configPath = flag.String("config", "", "File to read configuration from. Default: STDIN")
	outputPath = flag.String("output", "", "File to write configuration to. Default: STDOUT")
)

func main() {
	success, err := mainWithResult()
	if err != nil {
		log.Panic(err)
	}
	if !success {
		os.Exit(1)
	}
}

func mainWithResult() (bool, error) {
	flag.Parse()
	var conf config.Config
	if err := readConfig(&conf); err != nil {
		return false, err
	}

	collector := NewCollector()
	asyncHooks := NewAsyncHooks(collector)
	if err := StartCrawling(&conf, asyncHooks); err != nil {
		return false, err
	}
	asyncHooks.Wait()

	if err := writeResults(collector); err != nil {
		return false, err
	}

	return collector.IsSuccessful(), nil
}

func readConfig(config *config.Config) error {
	if *configPath == "" || *configPath == "-" {
		return config.FromSTDIN()
	}
	return config.FromFile(*configPath)
}

func writeResults(c *Collector) error {
	var writer io.Writer
	if *outputPath == "" || *outputPath == "-" {
		writer = os.Stdout
	} else {
		file, err := os.Create(*outputPath)
		if err != nil {
			return err
		}
		defer func() {
			err = file.Close()
			if err != nil {
				log.Printf("failed to close output file: %s", err)
			}
		}()
		writer = file
	}

	bufWriter := bufio.NewWriter(writer)
	if err := c.Write(bufWriter); err != nil {
		return err
	}
	return bufWriter.Flush()
}
