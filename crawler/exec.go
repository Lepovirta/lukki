package crawler

import (
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/report"
)

func Execute(conf *config.Config) (*report.Report, error) {
	events := make(chan interface{}, 1000)
	collector := newCollector()
	asyncCollector := newAsyncCollector(collector, events)
	go asyncCollector.collect()
	if err := crawl(conf, events); err != nil {
		return nil, err
	}
	asyncCollector.wait()
	return collector.report(), nil
}
