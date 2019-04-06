package crawler

import (
	"fmt"
	"io"
	"log"
)

type collector struct {
	Logs   map[ID]*logItem
	Errors []error
}

func newCollector() *collector {
	return &collector{
		Logs:   make(map[ID]*logItem, 1000),
		Errors: make([]error, 0, 100),
	}
}

func (c *collector) Start(r *request) {
	l := c.Logs[r.ID]
	if l == nil {
		c.Logs[r.ID] = &logItem{Request: r}
	} else if l.HasStarted() {
		log.Printf("URL processing already started: %s", r.URL)
	} else {
		l.Request = r
	}
}

func (c *collector) End(r *response) {
	l := c.Logs[r.ID]
	if l == nil {
		c.Logs[r.ID] = &logItem{Response: r}
	} else if l.HasEnded() {
		log.Printf("URL processing already ended: %s", r.URL)
	} else {
		l.Response = r
	}
}

func (c *collector) Error(err error) {
	c.Errors = append(c.Errors, err)
}

func (c *collector) Stop() {
	// NOOP
}

func (c *collector) Write(w io.Writer) error {
	if len(c.Logs) > 0 {
		if _, err := w.Write([]byte("Results:\n")); err != nil {
			return err
		}
		for _, l := range c.Logs {
			line := fmt.Sprintf("  %s\n", l)
			if _, err := w.Write([]byte(line)); err != nil {
				return err
			}
		}
	}
	if len(c.Errors) > 0 {
		if _, err := w.Write([]byte("Errors:\n")); err != nil {
			return err
		}
		for _, logErr := range c.Errors {
			line := fmt.Sprintf("  %s\n", logErr)
			if _, err := w.Write([]byte(line)); err != nil {
				return err
			}
		}
	}
	return nil
}

func (c *collector) IsSuccessful() bool {
	if len(c.Errors) > 0 {
		return false
	}

	for _, l := range c.Logs {
		if !l.IsSuccessful() {
			return false
		}
	}

	return true
}
