package main

import (
	"fmt"
	"github.com/Lepovirta/lukki/config"
	"strings"
	"time"

	"github.com/gocolly/colly"
)

type Hooks interface {
	Start(*Request)
	End(*Response)
	Error(error)
	Stop()
}

func StartCrawling(conf *config.Config, hooks Hooks) error {
	collector := colly.NewCollector()
	collector.IgnoreRobotsTxt = conf.IgnoreRobotsTxt
	collector.UserAgent = conf.UserAgent
	collector.AllowURLRevisit = false
	collector.ParseHTTPErrorResponse = true
	collector.Async = true

	if err := collector.Limit(&colly.LimitRule{
		DomainGlob:  "*",
		Parallelism: conf.Parallelism,
	}); err != nil {
		return err
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
					hooks.Error(fmt.Errorf(
						"failed to scan %s.%s='%s' at %s",
						element, attribute, attributeValue, e.Request.URL,
					))
				}
			}
		})
	}

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

	for _, url := range conf.URLs {
		if err := collector.Visit(url); err != nil {
			hooks.Error(err)
		}
	}
	collector.Wait()
	hooks.Stop()

	return nil
}

func isNotLocalLink(s string) bool {
	return !strings.HasPrefix(s, "#")
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
