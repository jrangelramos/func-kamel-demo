package function

import (
	"context"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"strings"
)

func Handle(ctx context.Context, res http.ResponseWriter, req *http.Request) {
	org := os.Getenv("GITHUB_ORG")
	repo := os.Getenv("GITHUB_REPO")
	token := os.Getenv("GITHUB_TOKEN")

	body, err := QueryIssues(org, repo, token)
	if err == nil {
		res.Write(body)
	} else {
		fmt.Println(err)
	}
}

func QueryIssues(org string, repo string, token string) (body []byte, err error) {
	url := fmt.Sprintf("https://api.github.com/repos/%s/%s/issues", org, repo)
	req, err := http.NewRequest("GET", url, strings.NewReader(""))
	if err != nil {
		return nil, err
	}
	req.Header.Add("Content-Type", "application/vnd.github.v3+json")
	req.Header.Add("Authorization", "Bearer "+token)
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	return ioutil.ReadAll(resp.Body)
}
