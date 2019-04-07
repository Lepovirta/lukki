package crawler

import (
	"github.com/Lepovirta/lukki/report"
	"log"
	"time"
)

type collector struct {
	startTime time.Time
	endTime   time.Time
	logs      map[ID]*logItem
	errors    []error
}

func newCollector() *collector {
	return &collector{
		logs:   make(map[ID]*logItem, 1000),
		errors: make([]error, 0, 100),
	}
}

func (c *collector) start(t startTime) {
	c.startTime = time.Time(t)
}

func (c *collector) request(r *request) {
	l := c.logs[r.ID]
	if l == nil {
		c.logs[r.ID] = &logItem{req: r}
	} else if l.hasStarted() {
		log.Printf("URL processing already started: %s", r.URL)
	} else {
		l.req = r
	}
}

func (c *collector) respond(r *response) {
	l := c.logs[r.ID]
	if l == nil {
		c.logs[r.ID] = &logItem{res: r}
	} else if l.hasEnded() {
		log.Printf("URL processing already ended: %s", r.URL)
	} else {
		l.res = r
	}
}

func (c *collector) error(err error) {
	c.errors = append(c.errors, err)
}

func (c *collector) stop(t endTime) {
	c.endTime = time.Time(t)
}

func (c *collector) report() *report.Report {
	resources := make([]*report.Resource, 0, len(c.logs))
	failedRequests := make([]*report.FailedRequest, 0, len(c.logs))
	errors := make([]string, 0, len(c.errors))

	for _, e := range c.errors {
		errors = append(errors, e.Error())
	}

	for _, l := range c.logs {
		if l.requestFailed() {
			failedRequests = append(failedRequests, &report.FailedRequest{
				URL:   l.req.URL.String(),
				Error: l.res.Error.Error(),
			})
		} else {
			resources = append(resources, &report.Resource{
				URL:        l.req.URL.String(),
				StatusCode: l.res.StatusCode,
				StartTime:  l.req.Timestamp,
				EndTime:    l.res.Timestamp,
			})
		}
	}

	return &report.Report{
		StartTime:      c.startTime,
		EndTime:        c.endTime,
		Resources:      resources,
		FailedRequests: failedRequests,
		Errors:         errors,
	}
}
