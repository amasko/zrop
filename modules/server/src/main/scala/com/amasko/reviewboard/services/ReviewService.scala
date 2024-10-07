package com.amasko.reviewboard
package services

import domain.data.Review
import repositories.ReviewRepo

import zio.*

trait ReviewService:
  def getReview(id: Long): Task[Option[Review]]
  def getReviews: Task[List[Review]]
  def create(review: Review): Task[Review]
  def update(review: Review): Task[Review]
  def delete(id: Long): Task[Review]
  
final case class  ReviewServiceLive(repo: ReviewRepo) extends ReviewService:
  override def getReview(id: Long): Task[Option[Review]] = repo.getById(id)
  override def getReviews: Task[List[Review]] = repo.getAll
  override def create(review: Review): Task[Review] = repo.create(review)
  override def update(review: Review): Task[Review] = repo.update(review.id, _ => review)
  override def delete(id: Long): Task[Review] = repo.delete(id)
  
object ReviewServiceLive:
  val layer: ZLayer[ReviewRepo, Nothing, ReviewServiceLive] = zio.ZLayer.fromFunction(ReviewServiceLive.apply)