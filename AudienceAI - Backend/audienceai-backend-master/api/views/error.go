package views

import (
	"errors"
	"net/http"

	"github.com/wI2L/jettison"

	"github.com/ATechnoHazard/audienceai-backend/pkg"
)

type ErrView struct {
	Message string `json:"message"`
	Status  int    `json:"status"`
}

var (
	ErrMethodNotAllowed = errors.New("error: Method is not allowed")
	ErrInvalidToken     = errors.New("error: Invalid Authorization token")
	ErrUserExists       = errors.New("error: User already exists")
)

var ErrHTTPStatusMap = map[string]int{
	pkg.ErrNotFound.Error():      http.StatusNotFound,
	pkg.ErrInvalidSlug.Error():   http.StatusBadRequest,
	pkg.ErrAlreadyExists.Error(): http.StatusConflict,
	pkg.ErrNotFound.Error():      http.StatusNotFound,
	pkg.ErrDatabase.Error():      http.StatusInternalServerError,
	pkg.ErrUnauthorized.Error():  http.StatusUnauthorized,
	pkg.ErrForbidden.Error():     http.StatusForbidden,
	ErrMethodNotAllowed.Error():  http.StatusMethodNotAllowed,
	ErrInvalidToken.Error():      http.StatusBadRequest,
	ErrUserExists.Error():        http.StatusConflict,
}

func Wrap(err error, w http.ResponseWriter) {
	w.Header().Add("Content-Type", "application/json; charset=utf-8")
	msg := err.Error()
	code := ErrHTTPStatusMap[msg]

	// If error code is not found
	// like a default case
	if code == 0 {
		code = http.StatusInternalServerError
	}

	w.WriteHeader(code)

	errView := ErrView{
		Message: msg,
		Status:  code,
	}

	data, _ := jettison.Marshal(errView)
	_, _ = w.Write(data)
}
