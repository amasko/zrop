package com.amasko.reviewboard
package services

import zio.*
import domain.data.{User, UserToken}
import repositories.UserRepo
import domain.errors.NotFoundException

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

trait UserService:
  def registerUser(email: String, passwd: String): Task[User]
  def verifyPassword(email: String, passwd: String): Task[User]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, passwd: String): Task[User]
  def generateToken(email: String, passwd: String): Task[UserToken]

case class UserServiceLive(userRepo: UserRepo, jwt: JWTService) extends UserService:
  import UserServiceLive.Hasher

  override def registerUser(email: String, passwd: String): Task[User] =
    userRepo.createUser(
      User(-0L, email, Hasher.hashGen(passwd))
    )

  override def verifyPassword(email: String, passwd: String): Task[User] =
    userRepo
      .findUserByEmail(email)
      .someOrFail(NotFoundException(s"User with email ${email} not found"))
      .flatMap(user =>
        if Hasher.validateHash(passwd, user.hashedPassword) then ZIO.succeed(user)
        else ZIO.fail(new IllegalArgumentException("Invalid password"))
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
        user <- verifyPassword(email, passwd)
        token <- jwt.createToken(user)
    yield token

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
