package upload

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"

	"github.com/ATechnoHazard/audienceai-backend/api/views"
	"github.com/ATechnoHazard/audienceai-backend/internal/utils"
	"github.com/ATechnoHazard/audienceai-backend/pkg/entities"
	"github.com/ATechnoHazard/audienceai-backend/pkg/status"
	"github.com/julienschmidt/httprouter"
	"github.com/wI2L/jettison"
)

func upVid(statSvc status.StatService) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Get file handle on multipart video
		file, handler, err := r.FormFile("video")
		fileName := r.FormValue("file_name")
		if err != nil {
			views.Wrap(err, w)
			return
		}
		defer file.Close()

		// Create file handle on local disk
		filePath := fmt.Sprintf("./videos/%s", handler.Filename)
		f, err := os.OpenFile(filePath, os.O_WRONLY|os.O_CREATE, 0744)
		if err != nil {
			views.Wrap(err, w)
			return
		}
		defer f.Close()

		// Copy multipart file to disk
		_, err = io.Copy(f, file)
		if err != nil {
			views.Wrap(err, w)
			return
		}

		// Create absolute file path
		filePath, err = filepath.Abs(filePath)
		if err != nil {
			views.Wrap(err, w)
			return
		}

		// Send a request to the AI service
		reqBody, _ := jettison.Marshal(map[string]interface{}{
			"video_path": filePath,
			"fps":        1,
		})
		resp, err := http.Post("http://localhost:5000/process_video", "application/json", bytes.NewBuffer(reqBody))
		if err != nil {
			views.Wrap(err, w)
		}
		defer resp.Body.Close()

		respBody := &views.VidServiceResponse{}
		if err = json.NewDecoder(resp.Body).Decode(respBody); err != nil {
			views.Wrap(err, w)
			return
		}

		if err := statSvc.SetStat(&entities.Status{
			FilePath: filePath,
			FileName: fileName,
			Status:   "Processing",
		}); err != nil {
			views.Wrap(err, w)
			return
		}

		// Send response to client
		msg := utils.Message(http.StatusAccepted, fmt.Sprintf("File %s uploaded successfully", fileName))
		utils.Respond(w, msg)
		return
	}
}

func setStat(statSvc status.StatService) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		stat := &entities.Status{}
		if err := json.NewDecoder(r.Body).Decode(stat); err != nil {
			views.Wrap(err, w)
			return
		}

		statDB, err := statSvc.GetProcessing()
		if err != nil {
			views.Wrap(err, w)
			return
		}
		emScJs, _ := jettison.Marshal(&entities.EmotionData{Data: stat.EmotionScores})

		statDB.EmotionScoresJson = string(emScJs)
		statDB.NumFrames = stat.NumFrames
		statDB.Status = "Processed"

		if err := statSvc.SetStat(statDB); err != nil {
			views.Wrap(err, w)
			return
		}

		msg := utils.Message(http.StatusOK, "Status successfully set")
		utils.Respond(w, msg)
		return
	}
}

func getStat(statSvc status.StatService) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		stat := &entities.Status{}
		if err := json.NewDecoder(r.Body).Decode(stat); err != nil {
			views.Wrap(err, w)
			return
		}

		stat, err := statSvc.GetStat(stat.FileName)
		if err != nil {
			views.Wrap(err, w)
			return
		}

		msg := utils.Message(http.StatusOK, "Successfully fetched status for file")
		msg["status"] = stat
		utils.Respond(w, msg)
		return
	}
}

func MakeUpload(r *httprouter.Router, statSvc status.StatService) {
	r.HandlerFunc("POST", "/api/upload", upVid(statSvc))
	r.HandlerFunc("POST", "/api/predictComplete", setStat(statSvc))
	r.HandlerFunc("POST", "/api/getStatus", getStat(statSvc))
}
