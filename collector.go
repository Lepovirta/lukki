package main

import (
	"fmt"
	"io"
	"log"
)

type Collector struct {
	Logs   map[ID]*LogItem
	Errors []error
}

func NewCollector() *Collector {
	return &Collector{
		Logs:   make(map[ID]*LogItem, 1000),
		Errors: make([]error, 0, 100),
	}
}

func (c *Collector) Start(r *Request) {
	l := c.Logs[r.ID]
	if l == nil {
		c.Logs[r.ID] = &LogItem{Request: r}
	} else if l.HasStarted() {
		log.Printf("URL processing already started: %s", r.URL)
	} else {
		l.Request = r
	}
}

func (c *Collector) End(r *Response) {
	l := c.Logs[r.ID]
	if l == nil {
		c.Logs[r.ID] = &LogItem{Response: r}
	} else if l.HasEnded() {
		log.Printf("URL processing already ended: %s", r.URL)
	} else {
		l.Response = r
	}
}

func (c *Collector) Error(err error) {
	c.Errors = append(c.Errors, err)
}

func (c *Collector) Stop() {
	// NOOP
}

func (c *Collector) Write(w io.Writer) error {
	if len(c.Logs) > 0 {
		if _, err := w.Write([]byte("Results:\n")); err != nil {
			return err
		}
		for _, log := range c.Logs {
			line := fmt.Sprintf("  %s\n", log)
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

func (c *Collector) IsSuccessful() bool {
	if len(c.Errors) > 0 {
		return false
	}

	for _, log := range c.Logs {
		if !log.IsSuccessful() {
			return false
		}
	}

	return true
}
