package main

import (
	"flag"
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/crawler"
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

	return crawler.Execute(&conf, *outputPath)
}

func readConfig(config *config.Config) error {
	if *configPath == "" || *configPath == "-" {
		return config.FromSTDIN()
	}
	return config.FromFile(*configPath)
}
