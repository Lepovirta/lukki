package format

import (
	"fmt"
	"github.com/Lepovirta/lukki/report"
	"io"
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
			line := fmt.Sprintf("  %s\n", logErr)
			if _, err := w.Write([]byte(line)); err != nil {
				return err
			}
		}
	}
	return nil
}
