package format

import (
	"fmt"
	"io"
	"strings"

	"github.com/Lepovirta/lukki/report"
)

func ToASCII(r *report.Report, w io.Writer) error {
	// Resources
	if len(r.Resources) > 0 {
		if _, err := w.Write([]byte("Results:\n")); err != nil {
			return err
		}
		for _, res := range r.Resources {
			line := fmt.Sprintf("  %s\n", res)
			if _, err := w.Write([]byte(line)); err != nil {
				return err
			}
		}
	}

	// Failed requests
	if len(r.FailedRequests) > 0 {
		if _, err := w.Write([]byte("Failed requests:\n")); err != nil {
			return err
		}
		for _, fr := range r.FailedRequests {
			line := fmt.Sprintf("  %s\n", fr)
			if _, err := w.Write([]byte(line)); err != nil {
				return err
			}
		}
	}

	// Errors
	if len(r.Errors) > 0 {
		if _, err := w.Write([]byte("Errors:\n")); err != nil {
			return err
		}
		for _, logErr := range r.Errors {
			errTxt := formatErrorTxt(logErr)
			if _, err := w.Write([]byte(errTxt)); err != nil {
				return err
			}
		}
	}

	// Summary
	duration := fmt.Sprintf("Crawl duration: %s\n", r.Duration())
	if _, err := w.Write([]byte(duration)); err != nil {
		return err
	}

	return nil
}

func formatErrorTxt(errTxt string) string {
	// Add indentation for multiline errors
	errTxt = strings.ReplaceAll(errTxt, "\n", "\n    ")
	
	// Remove the extra surrounding whitespace
	errTxt = strings.TrimSpace(errTxt)
	
	// Add initial indentation and ensure that there's a newline character
	return fmt.Sprintf("  %s\n", errTxt)
}
