package crawler

import (
	"fmt"
	"github.com/Lepovirta/lukki/config"
	"strings"
	"time"

	"github.com/gocolly/colly"
)

func crawl(conf *config.Config, events chan interface{}) error {
	collector := colly.NewCollector()
	collector.IgnoreRobotsTxt = conf.IgnoreRobotsTxt
	collector.UserAgent = conf.UserAgent
	collector.AllowURLRevisit = false
	collector.ParseHTTPErrorResponse = true
	collector.Async = true

	if conf.Parallelism > 0 {
		if err := collector.Limit(&colly.LimitRule{
			DomainGlob:  "*",
			Parallelism: conf.Parallelism,
		}); err != nil {
			return err
		}
	}

	homeHosts := conf.HomeHostsMap()

	for i := range conf.Elements {
		element := conf.Elements[i].Name
		attribute := conf.Elements[i].Attribute
		query := fmt.Sprintf("%s[%s]", element, attribute)

		collector.OnHTML(query, func(e *colly.HTMLElement) {
			attributeValue := e.Attr(attribute)
			if homeHosts[e.Request.URL.Hostname()] && isNotLocalLink(attributeValue) {
				err := e.Request.Visit(attributeValue)
				if err != nil && err != colly.ErrAlreadyVisited && err != colly.ErrRobotsTxtBlocked {
					events <- fmt.Errorf(
						"failed to scan %s.%s='%s' at %s",
						element, attribute, attributeValue, e.Request.URL,
					)
				}
			}
		})
	}

	collector.OnRequest(func(r *colly.Request) {
		request := &request{
			ID:        r.ID,
			Timestamp: time.Now(),
			URL:       r.URL,
		}
		events <- request
	})

	collector.OnResponse(func(r *colly.Response) {
		events <- collyResponseToResponse(r, nil)
	})

	collector.OnError(func(r *colly.Response, err error) {
		events <- collyResponseToResponse(r, err)
	})

	events <- startTime(time.Now())
	for _, url := range conf.URLs {
		if err := collector.Visit(url); err != nil {
			events <- err
		}
	}
	collector.Wait()
	events <- endTime(time.Now())

	return nil
}

func isNotLocalLink(s string) bool {
	return !strings.HasPrefix(s, "#")
}

func collyResponseToResponse(r *colly.Response, err error) *response {
	return &response{
		ID:         r.Request.ID,
		Timestamp:  time.Now(),
		URL:        r.Request.URL,
		StatusCode: r.StatusCode,
		Headers:    r.Headers,
		Body:       r.Body,
		Error:      err,
	}
}
