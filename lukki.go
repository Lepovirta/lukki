package main

import (
	"bufio"
	"flag"
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/crawler"
	"github.com/Lepovirta/lukki/format"
	"github.com/Lepovirta/lukki/internal/buildinfo"
	"github.com/Lepovirta/lukki/report"
	"io"
	"log"
	"os"
)

var (
	configPath   = flag.String("config", "STDIN", "File to read configuration from")
	outputPath   = flag.String("output", "STDOUT", "File to write configuration to")
	reportFormat = flag.String("format", "ascii", "Format of the report")
	printVersion = flag.Bool("version", false, "Print version information")
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
	if *printVersion {
		buildinfo.PrintVersion()
		return true, nil
	}

	var conf config.Config
	if err := readConfig(&conf); err != nil {
		return false, err
	}

	r, err := crawler.Execute(&conf)
	if err != nil {
		return false, err
	}

	if err := writeReport(r); err != nil {
		return false, err
	}

	return r.IsSuccessful(), nil
}

func readConfig(config *config.Config) error {
	if *configPath == "STDIN" || *configPath == "" || *configPath == "-" {
		return config.FromSTDIN()
	}
	return config.FromFile(*configPath)
}

func writeReport(r *report.Report) error {
	var writer io.Writer
	if *outputPath == "STDOUT" || *outputPath == "" || *outputPath == "-" {
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

	if err := format.ByType(*reportFormat, r, bufWriter); err != nil {
		return err
	}

	return bufWriter.Flush()
}
