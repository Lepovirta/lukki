package report

import (
	"testing"
	"github.com/stretchr/testify/assert"
)

func TestReportFailed(t *testing.T) {
	a := assert.New(t)

	resource1 := &Resource{StatusCode: 200}
	resource2 := &Resource{StatusCode: 400}
	reports := []struct{
		report *Report
		successful bool
	}{
		{&Report{Resources: []*Resource{resource1, resource2}}, false},
		{&Report{Resources: []*Resource{resource1}}, true},
		{&Report{Resources: []*Resource{resource2}}, false},
		{&Report{Errors: []string{"error!"}}, false},
		{&Report{FailedRequests: []*FailedRequest{&FailedRequest{Error: "error!"}}}, false},
	}

	a.True(resource1.IsSuccessful())
	a.False(resource2.IsSuccessful())
	for _, r := range reports {
		a.Equal(r.successful, r.report.IsSuccessful())
	}
}
