package models

trait BaseEntity[+PK] {
  def id : PK
}

