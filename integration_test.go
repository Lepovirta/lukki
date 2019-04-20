package main

import (
	"fmt"
	"github.com/Lepovirta/lukki/config"
	"github.com/Lepovirta/lukki/crawler"
	"github.com/Lepovirta/lukki/internal/testserver"
	"github.com/Lepovirta/lukki/report"
	"github.com/stretchr/testify/assert"
	"net"
	"os"
	"strings"
	"testing"
)

var serverAddress string

func TestMain(m *testing.M) {
	// listener with random port
	listener, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		panic(err)
	}
	defer func() {
		err := listener.Close()
		if err != nil {
			panic(err)
		}
	}()
	serverAddress = listener.Addr().String()

	// test server from testsite directory files
	go func() {
		err := testserver.Start("testsite", listener)
		if err != nil {
			panic(err)
		}
	}()

	// run tests
	exitCode := m.Run()
	os.Exit(exitCode)
}

func serverUrl(path string) string {
	if !strings.HasPrefix(path, "/") {
		path = "/" + path
	}
	return fmt.Sprintf("http://%s%s", serverAddress, path)
}

func createConfig(path string) (*config.CrawlConfig, error) {
	c := config.CrawlConfig{
		URLs: []string{serverUrl(path)},
	}
	return &c, c.Init()
}

func TestWorkingSite(t *testing.T) {
	a := assert.New(t)
	testConf, err := createConfig("/index.html")
	if err != nil {
		t.Fatal(err)
	}

	r, err := crawler.Execute(testConf)
	if err != nil {
		t.Fatal(err)
	}

	a.True(r.StartTime.Before(r.EndTime))
	a.True(r.IsSuccessful())
	a.False(r.IsFailed())
	a.Empty(r.Errors)
	a.Len(r.FailedRequests, 0)
	assertMatchesResources(
		t,
		r.Resources,
		[]func(*report.Resource) bool{
			matchResource("index.html", 200),
			matchResource("test1.html", 200),
			matchResource("test2.html", 200),
			matchResource("test3.html", 200),
			matchResource("image.png", 200),
		},
	)
}

func TestFailingSite(t *testing.T) {
	a := assert.New(t)
	testConf, err := createConfig("/fail.html")

	r, err := crawler.Execute(testConf)
	if err != nil {
		t.Fatal(err)
	}

	a.True(r.StartTime.Before(r.EndTime))
	a.False(r.IsSuccessful())
	a.True(r.IsFailed())
	a.Empty(r.Errors)
	a.Len(r.FailedRequests, 0)
	assertMatchesResources(
		t,
		r.Resources,
		[]func(*report.Resource) bool{
			matchResource("fail.html", 200),
			matchResource("test1.html", 200),
			matchResource("test2.html", 200),
			matchResource("test3.html", 200),
			matchResource("image.png", 200),
			matchResource("test404.html", 404),
			matchResource("failimage.jpg", 404),
		},
	)
}

func matchResource(path string, statusCode int) func(*report.Resource) bool {
	return func(r *report.Resource) bool {
		return r.URL == serverUrl(path) && r.StatusCode == statusCode
	}
}

func assertMatchesResources(t *testing.T, resources []*report.Resource, fs []func(*report.Resource) bool) {
	if len(resources) != len(fs) {
		t.Fatalf("resource count %d, expected %d", len(resources), len(fs))
	}
	for _, r := range resources {
		matchFound := false
		for _, f := range fs {
			if f(r) {
				matchFound = true
				break
			}
		}
		if !matchFound {
			t.Errorf("no match found for %+v", r)
		}
	}
}
