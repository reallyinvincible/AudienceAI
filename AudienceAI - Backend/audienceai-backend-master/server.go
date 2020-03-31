package main

import (
	"fmt"
	"github.com/ATechnoHazard/audienceai-backend/api/upload"
	"github.com/ATechnoHazard/audienceai-backend/internal/utils"
	"github.com/ATechnoHazard/audienceai-backend/pkg/entities"
	"github.com/ATechnoHazard/audienceai-backend/pkg/status"
	"github.com/jinzhu/gorm"
	"github.com/joho/godotenv"
	"github.com/julienschmidt/httprouter"
	"github.com/lib/pq"
	negronilogrus "github.com/meatballhat/negroni-logrus"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/negroni"
	"net/http"
	"os"
)

func init() {
	log.SetFormatter(&log.JSONFormatter{PrettyPrint: true})
	log.SetOutput(os.Stdout)
	log.Printf("Running on %s", os.Getenv("ENV"))
	if os.Getenv("ENV") != "PROD" {
		err := godotenv.Load()
		if err != nil {
			log.Fatal(err)
		}
	}
}

func initNegroni() *negroni.Negroni {
	n := negroni.New()
	n.Use(negronilogrus.NewCustomMiddleware(log.DebugLevel, &log.JSONFormatter{PrettyPrint: true}, "API requests"))
	n.Use(negroni.NewRecovery())
	return n
}

func connectDb() *gorm.DB {
	conn, err := pq.ParseURL(os.Getenv("DB_URI"))
	if err != nil {
		log.Fatal(err)
	}

	db, err := gorm.Open("postgres", conn)
	if err != nil {
		log.Fatal(err)
	}

	if os.Getenv("DEBUG") == "true" {
		db = db.Debug()
	}

	db.AutoMigrate(&entities.Status{})
	return db
}

func main() {
	r := httprouter.New()
	n := initNegroni()
	n.UseHandler(r)
	db := connectDb()
	base := os.Getenv("BASE_PATH")

	r.HandlerFunc("GET", base+"/", func(w http.ResponseWriter, r *http.Request) {
		msg := utils.Message(http.StatusOK, "OK")
		utils.Respond(w, msg)
	})

	statSvc := status.NewStatService(db)
	upload.MakeUpload(r, statSvc)

	// listen and serve on given port
	port := os.Getenv("PORT")
	if port == "" {
		port = "4000"
	}

	log.WithField("event", "START").Info("Listening on port " + port)

	log.Panic(http.ListenAndServe(fmt.Sprintf(":%s", port), n))
}
