package config

import (
	"bufio"
	"encoding/json"
	"io"
	"log"
	"net/url"
	"os"
)

const (
	userAgent = "lukkibot"
)

var (
	defaultElements = []Element{
		{"a", "href"},
		{"link", "href"},
		{"img", "src"},
		{"script", "src"},
	}
)

type Config struct {
	URLs            []string  `json:"urls"`
	HomeHosts       []string  `json:"homeHosts"`
	UserAgent       string    `json:"userAgent"`
	IgnoreRobotsTxt bool      `json:"ignoreRobotsTxt"`
	Parallelism     int       `json:"parallelism"`
	Elements        []Element `json:"elements"`
}

type Element struct {
	Name      string `json:"name"`
	Attribute string `json:"attribute"`
}

func (c *Config) FromJSON(input io.Reader) error {
	if err := json.NewDecoder(input).Decode(c); err != nil {
		return err
	}
	return c.Init()
}

func (c *Config) FromSTDIN() error {
	reader := bufio.NewReader(os.Stdin)
	return c.FromJSON(reader)
}

func (c *Config) FromFile(filename string) error {
	file, err := os.Open(filename)
	if err != nil {
		return err
	}
	defer func() {
		err := file.Close()
		if err != nil {
			log.Printf("failed to close input file: %s", err)
		}
	}()
	return c.FromJSON(bufio.NewReader(file))
}

func (c *Config) Init() (err error) {
	if c.Parallelism <= 0 {
		c.Parallelism = 4
	}
	if len(c.Elements) == 0 {
		c.Elements = defaultElements
	}
	if c.UserAgent == "" {
		c.UserAgent = userAgent
	}
	if len(c.HomeHosts) == 0 {
		c.HomeHosts, err = extractHosts(c.URLs)
	}
	return err
}

func extractHosts(urls []string) ([]string, error) {
	hosts := make(map[string]bool, len(urls))
	for _, urlString := range urls {
		u, err := url.Parse(urlString)
		if err != nil {
			return nil, err
		}
		hosts[u.Hostname()] = true
	}
	hostsSlice := make([]string, 0, len(hosts))
	for h := range hosts {
		hostsSlice = append(hostsSlice, h)
	}
	return hostsSlice, nil
}

func (c *Config) HomeHostsMap() map[string]bool {
	hosts := make(map[string]bool, len(c.HomeHosts))
	for _, host := range c.HomeHosts {
		hosts[host] = true
	}
	return hosts
}
