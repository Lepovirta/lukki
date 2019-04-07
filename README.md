# Lukki

Static website testing through crawling.
Point Lukki to a website and it'll find broken pages for you.

## Downloading

You can find all downloadable versions from the
[releases page](https://github.com/Lepovirta/lukki/releases).

## Usage

Lukki accepts the following parameters:

* `-config` (string): File to read configuration from (default "STDIN")
* `-format` (string): Format of the report (default "ascii")
* `-output` (string): File to write configuration to (default "STDOUT")
* `-version`: Print version information

## Configuration

The configuration is provided in JSON format.
These are the accepted configurations:

* `urls` (list of strings):
  List of URLs to start crawling from
* `homeHosts` (list of strings, optional):
  List of hosts that Lukki will crawl through.
  This can be used to prevent Lukki from crawling external sites.
  If not set, the hosts from `urls` parameter will be used.
* `userAgent` (string, optional):
  The user agent the crawler will use
* `ignoreRobotsTxt` (boolean, optional):
  Whether or not Lukki should ignore `robots.txt` directives it finds.
  Default: `true`
* `parallelism` (integer, optional):
  Number of concurrent workers used for crawling. Default: 4
* `elements` (list of maps, optional):
  Which HTML elements to find links from.
  Each element should contain a `name` for the HTML element name (e.g. `a`),
  and `attribute` for HTML element attribute (e.g. `href`).
  Default elements: `a.href`, `link.href`, `img.src`, `script.src`.

## License

GNU General Public License v3.0

See LICENSE file for more information.
