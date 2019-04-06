package crawler

import (
	"net/http"
	"net/url"
	"time"
)

type ID = uint32

type startTime time.Time
type endTime time.Time

type logItem struct {
	req *request
	res *response
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

func (l *logItem) hasStarted() bool {
	return l.req != nil
}

func (l *logItem) hasEnded() bool {
	return l.res != nil
}

func (l *logItem) requestFailed() bool {
	return l.res.Error != nil
}
