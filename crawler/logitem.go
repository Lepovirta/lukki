package crawler

import (
	"fmt"
	"net/http"
	"net/url"
	"time"
)

type ID = uint32

type logItem struct {
	Request  *request
	Response *response
}

type request struct {
	ID        ID
	Timestamp time.Time
	URL       *url.URL
}

type response struct {
	ID         ID
	Timestamp  time.Time
	URL        *url.URL
	StatusCode int
	Headers    *http.Header
	Body       []byte
	Error      error
}

func (l *logItem) HasStarted() bool {
	return l.Request != nil
}

func (l *logItem) HasEnded() bool {
	return l.Response != nil
}

func (l *logItem) String() string {
	if !l.HasStarted() {
		return fmt.Sprintf("incomplete: %s [NO REQUEST]", l.Response.URL.String())
	}
	if !l.HasEnded() {
		return fmt.Sprintf("incomplete: %s [NO RESPONSE]", l.Request.URL.String())
	}

	var status string
	if l.IsSuccessful() {
		status = "OK  "
	} else {
		status = "FAIL"
	}

	var statusCode string
	if l.Response.StatusCode == 0 && l.Response.Error != nil {
		statusCode = "ERR"
	} else {
		statusCode = fmt.Sprintf("%d", l.Response.StatusCode)
	}

	urlStr := l.Request.URL.String()

	var errStr string
	if l.Response.Error != nil {
		errStr = ": " + l.Response.Error.Error()
	}

	return fmt.Sprintf(
		"%s [%s] (%dms) %s%s",
		status, statusCode, l.Duration()/time.Millisecond, urlStr, errStr,
	)
}

func (l *logItem) IsSuccessful() bool {
	return l.Response.Error == nil && l.Response.StatusCode >= 200 && l.Response.StatusCode < 400
}

func (l *logItem) Duration() time.Duration {
	return l.Response.Timestamp.Sub(l.Request.Timestamp)
}
