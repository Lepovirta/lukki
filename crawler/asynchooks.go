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

func (a *asyncHooks) Start(t startTime) {
	a.ch <- t
}

func (a *asyncHooks) Request(r *request) {
	a.ch <- r
}

func (a *asyncHooks) Respond(r *response) {
	a.ch <- r
}

func (a *asyncHooks) Error(err error) {
	a.ch <- err
}

func (a *asyncHooks) Stop(t endTime) {
	a.ch <- t
}

func (a *asyncHooks) Wait() {
	for {
		uv := <-a.ch
		switch v := uv.(type) {
		case startTime:
			a.hs.Start(v)
		case *request:
			a.hs.Request(v)
		case *response:
			a.hs.Respond(v)
		case error:
			a.hs.Error(v)
		case endTime:
			a.hs.Stop(v)
			return
		default:
			log.Panicf("unknown type received: %T", v)
		}
	}
}
