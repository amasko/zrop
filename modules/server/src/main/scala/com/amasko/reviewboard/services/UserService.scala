package com.amasko.reviewboard
package services

import zio.*
import domain.data.{User, UserToken}
import repositories.{RecoveryTokensRepo, UserRepo}
import domain.errors.NotFoundException
import domain.errors.UnauthorizedException

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

trait UserService:
  def registerUser(email: String, passwd: String): Task[User]
  def verifyPassword(email: String, passwd: String): Task[User]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, passwd: String): Task[User]
  def generateToken(email: String, passwd: String): Task[UserToken]
  def sendPasswordRecoveryToken(email: String): Task[Unit]
  def recoverPassword(email: String, token: String, newPassword: String): Task[Boolean]

case class UserServiceLive(
    userRepo: UserRepo,
    jwt: JWTService,
    emailService: EmailService,
    tokenRepo: RecoveryTokensRepo
) extends UserService:
  import UserServiceLive.Hasher

  override def registerUser(email: String, passwd: String): Task[User] =
    userRepo.createUser(
      User(-0L, email, Hasher.hashGen(passwd))
    )

  private def findUserOrFail(email: String): Task[User] =
    userRepo
      .findUserByEmail(email)
      .someOrFail(NotFoundException(s"User with email ${email} not found"))

  override def verifyPassword(email: String, passwd: String): Task[User] =
    findUserOrFail(email)
      .flatMap(user =>
        if Hasher.validateHash(passwd, user.hashedPassword) then ZIO.succeed(user)
        else ZIO.fail(UnauthorizedException)
      )

  override def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User] =
    for
      user    <- verifyPassword(email, oldPassword)
      updated <- userRepo.updateUser(user.id, _.copy(hashedPassword = Hasher.hashGen(newPassword)))
    yield updated

  override def deleteUser(email: String, passwd: String): Task[User] =
    for
      user    <- verifyPassword(email, passwd)
      deleted <- userRepo.deleteUser(user.id)
    yield deleted

  override def generateToken(email: String, passwd: String): Task[UserToken] =
    for
      user  <- verifyPassword(email, passwd)
      token <- jwt.createToken(user)
    yield token

  override def sendPasswordRecoveryToken(email: String): Task[Unit] =
    for
      user <- findUserOrFail(email)
//      token <- tokenRepo.getRecoveryToken(email)  todo why?
      token <- tokenRepo.createOrReplaceRecoveryToken(email)
      _     <- emailService.sendRecoveryToken(email, token) // do not allow sending to many emails
    yield ()

  override def recoverPassword(email: String, token: String, newPassword: String): Task[Boolean] =
    for
      user  <- findUserOrFail(email)
      valid <- tokenRepo.checkToken(email, token)
      _ <-
        if valid
        then userRepo.updateUser(user.id, _.copy(hashedPassword = Hasher.hashGen(newPassword)))
        else ZIO.fail(new IllegalArgumentException("Invalid token"))
      _ <- tokenRepo.deleteRecoveryToken(email)
    yield valid

end UserServiceLive

object UserServiceLive:
  val layer = ZLayer.fromFunction(UserServiceLive.apply)

  object Hasher:
    private val PBKDF2_iter = 1000
    private val PBKDF2_algo = "PBKDF2WithHmacSHA512"
    private val SALT_Bytes  = 24
    private val HASH_Bytes  = 24
    private val skf         = SecretKeyFactory.getInstance(PBKDF2_algo)

    def hashGen(passwd: String): String =
      val rng  = new SecureRandom()
      val salt = Array.ofDim[Byte](SALT_Bytes)
      rng.nextBytes(salt)
      val hashBytes = encode(
        passwd.toCharArray,
        salt,
        PBKDF2_iter,
        HASH_Bytes
      )
      s"$PBKDF2_iter:${toHex(salt)}:${toHex(hashBytes)}"

    def validateHash(passwd: String, userHashedPass: String): Boolean =
      val parts = userHashedPass.split(":")
      val iter  = parts(0).toInt
      val salt  = fromHex(parts(1))
      val hash  = fromHex(parts(2))
      val testHash = encode(
        passwd.toCharArray,
        salt,
        iter,
        HASH_Bytes
      )
      hash.sameElements(testHash)

    private def fromHex(hex: String): Array[Byte] =
      hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

    private def encode(
        passwd: Array[Char],
        salt: Array[Byte],
        iterations: Int,
        nBytes: Int
    ): Array[Byte] =
      skf
        .generateSecret(
          new PBEKeySpec(
            passwd,
            salt,
            iterations,
            nBytes * 8
          )
        )
        .getEncoded

    private def toHex(bytes: Array[Byte]): String = bytes.map("%02x".format(_)).mkString
