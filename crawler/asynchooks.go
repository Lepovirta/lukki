package crawler

import (
	"log"
)

type asyncHooks struct {
	hs hooks
	ch chan interface{}
}

func newAsyncHooks(syncHooks hooks) *asyncHooks {
	return &asyncHooks{
		hs: syncHooks,
		ch: make(chan interface{}, 1000),
	}
}

func (a *asyncHooks) Start(r *request) {
	a.ch <- r
}

func (a *asyncHooks) End(r *response) {
	a.ch <- r
}

func (a *asyncHooks) Error(err error) {
	a.ch <- err
}

func (a *asyncHooks) Stop() {
	a.ch <- "stop"
}

func (a *asyncHooks) Wait() {
	for {
		uv := <-a.ch
		switch v := uv.(type) {
		case *request:
			a.hs.Start(v)
		case *response:
			a.hs.End(v)
		case error:
			a.hs.Error(v)
		case string:
			if v == "stop" {
				a.hs.Stop()
				return
			}
		default:
			log.Panicf("unknown type received: %T", v)
		}
	}
}
