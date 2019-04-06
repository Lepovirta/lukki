package main

import (
	"fmt"
	"time"

	"github.com/gocolly/colly"
)

type Hooks interface {
	Start(*Request)
	End(*Response)
	Error(error)
	Stop()
}

func StartCrawler(config *Config, hooks Hooks) {
	collector := colly.NewCollector()
	collector.IgnoreRobotsTxt = config.IgnoreRobotsTxt
	collector.UserAgent = config.UserAgent
	collector.AllowURLRevisit = false
	collector.ParseHTTPErrorResponse = true
	collector.Async = true

	collector.Limit(&colly.LimitRule{
		DomainGlob:  "*",
		Parallelism: config.Parallelism,
	})

	homeHosts := config.HomeHostsMap()
	visitHTML := func(element, attribute string) {
		query := fmt.Sprintf("%s[%s]", element, attribute)
		collector.OnHTML(query, func(e *colly.HTMLElement) {
			if homeHosts[e.Request.URL.Hostname()] {
				e.Request.Visit(e.Attr(attribute))
			}
		})
	}

	visitHTML("a", "href")
	visitHTML("link", "href")
	visitHTML("img", "src")
	visitHTML("script", "src")

	collector.OnRequest(func(r *colly.Request) {
		request := &Request{
			ID:        r.ID,
			Timestamp: time.Now(),
			URL:       r.URL,
		}
		hooks.Start(request)
	})

	collector.OnResponse(func(r *colly.Response) {
		hooks.End(collyResponseToResponse(r, nil))
	})

	collector.OnError(func(r *colly.Response, err error) {
		hooks.End(collyResponseToResponse(r, err))
	})

	for _, url := range config.URLs {
		if err := collector.Visit(url); err != nil {
			hooks.Error(err)
		}
	}
	collector.Wait()
	hooks.Stop()
}

func collyResponseToResponse(r *colly.Response, err error) *Response {
	return &Response{
		ID:         r.Request.ID,
		Timestamp:  time.Now(),
		URL:        r.Request.URL,
		StatusCode: r.StatusCode,
		Headers:    r.Headers,
		Body:       r.Body,
		Error:      err,
	}
}
