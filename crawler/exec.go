package crawler

import (
	"bufio"
	"github.com/Lepovirta/lukki/config"
	"io"
	"log"
	"os"
)

func Execute(conf *config.Config, outputPath string) (bool, error) {
	collector := newCollector()
	asyncHooks := newAsyncHooks(collector)
	if err := crawl(conf, asyncHooks); err != nil {
		return false, err
	}
	asyncHooks.Wait()

	if err := writeResults(collector, outputPath); err != nil {
		return false, err
	}

	return collector.IsSuccessful(), nil
}

func writeResults(c *collector, outputPath string) error {
	var writer io.Writer
	if outputPath == "" || outputPath == "-" {
		writer = os.Stdout
	} else {
		file, err := os.Create(outputPath)
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
