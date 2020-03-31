package status

import (
	"github.com/ATechnoHazard/audienceai-backend/pkg"
	"github.com/ATechnoHazard/audienceai-backend/pkg/entities"
	"github.com/jinzhu/gorm"
)

type StatService interface {
	SetStat(status *entities.Status) error
	GetStat(fileName string) (*entities.Status, error)
	GetProcessing() (*entities.Status, error)
}

type statSvc struct {
	db *gorm.DB
}

func NewStatService(db *gorm.DB) StatService {
	return &statSvc{db: db}
}

func (s *statSvc) SetStat(status *entities.Status) error {
	tx := s.db.Begin()
	err := tx.Save(status).Error
	if err != nil {
		tx.Rollback()
		switch err {
		case gorm.ErrRecordNotFound:
			return pkg.ErrNotFound
		default:
			return pkg.ErrDatabase
		}
	}

	tx.Commit()
	return nil
}

func (s *statSvc) GetStat(fileName string) (*entities.Status, error) {
	tx := s.db.Begin()
	stat := &entities.Status{}
	err := tx.Where("file_name = ?", fileName).Find(stat).Error
	if err != nil {
		tx.Rollback()
		switch err {
		case gorm.ErrRecordNotFound:
			return nil, pkg.ErrNotFound
		default:
			return nil, pkg.ErrDatabase
		}
	}

	tx.Commit()
	return stat, nil
}

func (s *statSvc) GetProcessing() (*entities.Status, error) {
	tx := s.db.Begin()
	stat := &entities.Status{}
	err := tx.Where("status = ?", "Processing").Find(stat).Error
	if err != nil {
		tx.Rollback()
		switch err {
		case gorm.ErrRecordNotFound:
			return nil, pkg.ErrNotFound
		default:
			return nil, pkg.ErrDatabase
		}
	}

	tx.Commit()
	return stat, nil
}