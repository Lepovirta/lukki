package main

import (
	"bufio"
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/crawler"
	"github.com/Lepovirta/lukki/format"
	"github.com/Lepovirta/lukki/internal/buildinfo"
	"github.com/Lepovirta/lukki/report"
	"github.com/tucnak/climax"
	"io"
	"log"
	"os"
)

func main() {
	app := climax.New("lukki")
	app.Brief = "Lukki - A website tester"
	app.Version = buildinfo.Version
	app.AddCommand(climax.Command{
		Name:  "test",
		Brief: "Test a website",
		Help:  `Test a website by crawling the website as specified in the given configuration file.`,
		Flags: []climax.Flag{
			{
				Name:     "config",
				Short:    "c",
				Help:     "File to read the configuration from (default: STDIN)",
				Usage:    `--config=config.json`,
				Variable: true,
			},
			{
				Name:     "output",
				Short:    "o",
				Usage:    `--output=report.json`,
				Help:     "File to write the output to (default: STDOUT)",
				Variable: true,
			},
			{
				Name:     "format",
				Short:    "f",
				Usage:    `--format=ascii`,
				Help:     "Report format (choices: json, ascii) (default: json)",
				Variable: true,
			},
		},
		Examples: []climax.Example{
			{
				Usecase:     "--config=config.json --output=report.txt --format=ascii",
				Description: `Read config from "config.json", write report to "report.txt" in ASCII format`,
			},
			{
				Usecase:     "--format=ascii",
				Description: `Read config from STDIN, write report to STDOUT in ASCII format`,
			},
		},
		Handle: testWebsite,
	})
	app.AddCommand(climax.Command{
		Name:  "format",
		Brief: "Format a JSON report",
		Help:  `Convert a JSON report into another format`,
		Flags: []climax.Flag{
			{
				Name:     "input",
				Short:    "i",
				Help:     "File to read the report from (default: STDIN)",
				Usage:    `--input=input.json`,
				Variable: true,
			},
			{
				Name:     "output",
				Short:    "o",
				Usage:    `--output=report.txt`,
				Help:     "File to write the output to (default: STDOUT)",
				Variable: true,
			},
			{
				Name:     "format",
				Short:    "f",
				Usage:    `--format=ascii`,
				Help:     "Report format (choices: json, ascii) (default: ascii)",
				Variable: true,
			},
			{
				Name:     "use-report-exit-code",
				Short:    "e",
				Usage:    `--use-report-exit-code`,
				Help:     "Use report status as the exit code.",
				Variable: false,
			},
		},
		Examples: []climax.Example{
			{
				Usecase:     "--input=report.json --output=report.txt --format=ascii",
				Description: `Read report from "report.json", write report to "report.txt" in ASCII format`,
			},
			{
				Usecase:     "--format=ascii",
				Description: `Read report from STDIN, write report to STDOUT in ASCII format`,
			},
			{
				Usecase:     "--format=ascii --use-report-exit-code",
				Description: `Format in ASCII and use the report status as the exit code`,
			},
			{
				Usecase:     "--use-report-exit-code",
				Description: `Check if the reported test was successful or failed`,
			},
		},
		Handle: formatReport,
	})
	app.AddCommand(climax.Command{
		Name:   "version",
		Brief:  "Print version",
		Handle: printVersion,
	})
	os.Exit(app.Run())
}

func printVersion(_ climax.Context) int {
	buildinfo.PrintVersion()
	return 0
}

func testWebsite(ctx climax.Context) int {
	var conf config.CrawlConfig
	configPath, _ := ctx.Get("config")
	outputPath, _ := ctx.Get("output")
	reportFormat, ok := ctx.Get("format")
	if !ok {
		reportFormat = "json"
	}

	if err := readConfig(&conf, configPath); err != nil {
		log.Panic(err)
	}
	r, err := crawler.Execute(&conf)
	if err != nil {
		log.Panic(err)
	}
	if err := writeReport(r, outputPath, reportFormat); err != nil {
		log.Printf("failed to write report: %s", err)
		return 1
	}

	if r.IsSuccessful() {
		return 0
	}
	return 1
}

func readConfig(config *config.CrawlConfig, configPath string) error {
	if isSTDIN(configPath) {
		return config.FromSTDIN()
	}
	return config.FromFile(configPath)
}

func isSTDIN(path string) bool {
	return path == "STDIN" || path == "" || path == "-"
}

func isSTDOUT(path string) bool {
	return path == "STDOUT" || path == "" || path == "-"
}

func formatReport(ctx climax.Context) int {
	inputPath, _ := ctx.Get("input")
	outputPath, _ := ctx.Get("output")
	reportFormat, ok := ctx.Get("format")
	if !ok {
		reportFormat = "ascii"
	}
	useReportExitCode := ctx.Is("use-report-exit-code")

	r, err := readReport(inputPath)
	if err != nil {
		log.Printf("failed to read report JSON: %s", err)
		return 1
	}
	if err := writeReport(r, outputPath, reportFormat); err != nil {
		log.Printf("failed to write report: %s", err)
	}

	if useReportExitCode && r.IsFailed() {
		return 1
	}
	return 0
}

func readReport(inputPath string) (*report.Report, error) {
	var r report.Report
	var reader io.Reader
	if isSTDIN(inputPath) {
		reader = os.Stdin
	} else {
		file, err := os.Open(inputPath)
		if err != nil {
			return nil, err
		}
		defer func() {
			err := file.Close()
			if err != nil {
				log.Printf("failed to close input file: %s", err)
			}
		}()
		reader = file
	}

	bufReader := bufio.NewReader(reader)
	return &r, r.ReadFromJSON(bufReader)
}

func writeReport(r *report.Report, outputPath, reportFormat string) error {
	var writer io.Writer
	if isSTDOUT(outputPath) {
		writer = os.Stdout
	} else {
		file, err := os.Create(outputPath)
		if err != nil {
			return err
		}
		defer func() {
			err := file.Close()
			if err != nil {
				log.Printf("failed to close output file: %s", err)
			}
		}()
		writer = file
	}

	bufWriter := bufio.NewWriter(writer)
	if err := format.ByType(reportFormat, r, bufWriter); err != nil {
		return err
	}
	return bufWriter.Flush()
}
