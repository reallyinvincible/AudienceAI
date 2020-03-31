package utils

import (
	"net/http"

	"github.com/wI2L/jettison"
)

func Message(status int, message string) map[string]interface{} {
	return map[string]interface{}{"code": status, "message": message}
}

func Respond(w http.ResponseWriter, data map[string]interface{}) {
	w.Header().Add("Content-Type", "application/json; charset=utf-8")
	d, _ := jettison.Marshal(data)
	_, _ = w.Write(d)
}