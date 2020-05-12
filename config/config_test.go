package config

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestConfigInit(t *testing.T) {
	a := assert.New(t)

	conf := CrawlConfig{URLs: []string{"http://localhost:1313/", "https://lepovirta.org/"}}
	err := conf.Init()

	a.NoError(err)
	a.Equal(defaultElements, conf.Elements)
	a.ElementsMatch([]string{"localhost", "lepovirta.org"}, conf.HomeHosts)
	a.Equal(userAgent, conf.UserAgent)
}
