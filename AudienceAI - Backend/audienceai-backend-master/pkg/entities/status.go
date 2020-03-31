package entities

type Status struct {
	FilePath          string      `json:"file_path"`
	FileName          string      `json:"file_name" gorm:"primary_key"`
	Status            string      `json:"status"`
	EmotionScoresJson string      `json:"emotion_scores_json"`
	NumFrames         int         `json:"num_frames"`
	EmotionScores     [][]float64 `json:"emotion_scores" gorm:"-"`
}

type EmotionData struct {
	Data [][]float64 `json:"data"`
}
