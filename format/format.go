package format

import (
	"encoding/json"
	"errors"
	"github.com/Lepovirta/lukki/report"
	"io"
	"strings"
)

var (
	ErrUnknownType = errors.New("Unknown format type")
)

func ByType(t string, report *report.Report, output io.Writer) error {
	switch strings.ToLower(t) {
	case "json":
		return json.NewEncoder(output).Encode(report)
	case "ascii":
		return ToASCII(report, output)
	default:
		return ErrUnknownType
	}
}
