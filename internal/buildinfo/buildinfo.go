package buildinfo

import "fmt"

var Version string

func PrintVersion() {
	if Version == "" {
		fmt.Println("<no version>")
	} else {
		fmt.Println(Version)
	}
}
