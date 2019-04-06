package report

import (
	"fmt"
	"time"
)

type Report struct {
	Resources      []*Resource      `json:"resources"`
	FailedRequests []*FailedRequest `json:"failedRequests"`
	Errors         []string         `json:"errors"`
}

func (r *Report) IsSuccessful() bool {
	return !r.IsFailed()
}

func (r *Report) IsFailed() bool {
	if len(r.Errors) > 0 || len(r.FailedRequests) > 0 {
		return true
	}

	for _, r := range r.Resources {
		if r.IsFailed() {
			return true
		}
	}

	return false
}

type Resource struct {
	URL        string    `json:"url"`
	StatusCode int       `json:"statusCode"`
	StartTime  time.Time `json:"startTime"`
	EndTime    time.Time `json:"endTime"`
}

func (r *Resource) IsSuccessful() bool {
	return !r.IsFailed()
}

func (r *Resource) String() string {
	var status string
	if r.IsSuccessful() {
		status = "OK  "
	} else {
		status = "FAIL"
	}

	return fmt.Sprintf(
		"%s [%d] (%dms) %s",
		status, r.StatusCode, r.Duration()/time.Millisecond, r.URL,
	)
}

func (r *Resource) IsFailed() bool {
	return isFailedStatusCode(r.StatusCode)
}

func (r *Resource) Duration() time.Duration {
	return r.EndTime.Sub(r.StartTime)
}

func isFailedStatusCode(code int) bool {
	return code < 200 && code >= 400
}

type FailedRequest struct {
	URL   string `json:"url"`
	Error string `json:"error"`
}

func (fr *FailedRequest) String() string {
	return fmt.Sprintf("%s: %s", fr.URL, fr.Error)
}
