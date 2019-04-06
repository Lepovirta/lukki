package main

import (
	"log"
)

type AsyncHooks struct {
	hooks Hooks
	ch    chan interface{}
}

func NewAsyncHooks(syncHooks Hooks) *AsyncHooks {
	return &AsyncHooks{
		hooks: syncHooks,
		ch:    make(chan interface{}, 1000),
	}
}

func (a *AsyncHooks) Start(r *Request) {
	a.ch <- r
}

func (a *AsyncHooks) End(r *Response) {
	a.ch <- r
}

func (a *AsyncHooks) Error(err error) {
	a.ch <- err
}

func (a *AsyncHooks) Stop() {
	a.ch <- "stop"
}

func (a *AsyncHooks) Wait() {
	for {
		uv := <-a.ch
		switch v := uv.(type) {
		case *Request:
			a.hooks.Start(v)
		case *Response:
			a.hooks.End(v)
		case error:
			a.hooks.Error(v)
		case string:
			if v == "stop" {
				a.hooks.Stop()
				return
			}
		default:
			log.Panicf("unknown type received: %T", v)
		}
	}
}
