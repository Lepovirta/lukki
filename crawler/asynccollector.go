package crawler

import "log"

type asyncCollector struct {
	collector *collector
	events    chan interface{}
	done      chan int
}

func newAsyncCollector(collector *collector, events chan interface{}) *asyncCollector {
	return &asyncCollector{
		collector: collector,
		events:    events,
		done:      make(chan int),
	}
}

func (a *asyncCollector) collect() {
	for {
		uv := <-a.events
		switch v := uv.(type) {
		case startTime:
			a.collector.start(v)
		case *request:
			a.collector.request(v)
		case *response:
			a.collector.respond(v)
		case error:
			a.collector.error(v)
		case endTime:
			a.collector.stop(v)
			a.done <- 1
			return
		default:
			log.Panicf("unknown type received: %T", v)
		}
	}
}

func (a *asyncCollector) wait() {
	_ = <-a.done
}
