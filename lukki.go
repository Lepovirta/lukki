package main

import (
	"flag"
	"log"
	"os"
	"io"
	"bufio"
	"encoding/json"
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
	var config Config
	if err := readConfig(&config); err != nil {
		return false, err
	}
	config.Init()

	collector := NewCollector()
	asyncHooks := NewAsyncHooks(collector)
	StartCrawler(&config, asyncHooks)
	asyncHooks.Wait()

	if err := writeResults(collector); err != nil {
		return false, err
	}

	return collector.IsSuccessful(), nil
}

func readConfig(config *Config) error {
	var reader io.Reader
	if *configPath == "" || *configPath == "-" {
		reader = os.Stdin
	} else {
		file, err := os.Open(*configPath)
		if err != nil {
			return err
		}
		defer func() {
			err := file.Close()
			if err != nil {
				log.Printf("failed to close input file: %s", err)
			}
		}()
		reader = file		
	}

	return json.NewDecoder(bufio.NewReader(reader)).Decode(config)
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

