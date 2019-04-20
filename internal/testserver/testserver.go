package testserver

import (
	"net"
	"net/http"
)

func Start(path string, listener net.Listener) error {
	mux := http.NewServeMux()
	fs := http.FileServer(http.Dir(path))
	mux.Handle("/", fs)
	return http.Serve(listener, mux)
}
