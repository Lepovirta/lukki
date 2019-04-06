package crawler

import (
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/report"
)

func Execute(conf *config.Config) (*report.Report, error) {
	collector := newCollector()
	asyncHooks := newAsyncHooks(collector)
	if err := crawl(conf, asyncHooks); err != nil {
		return nil, err
	}
	asyncHooks.Wait()
	return collector.report(), nil
}
