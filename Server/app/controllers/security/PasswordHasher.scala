package controllers.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
  def hashPassword(plainText: String) = BCrypt.hashpw(plainText, BCrypt.gensalt)
  def checkPassword(plainText: String, encryptedPassword: String) =
    BCrypt.checkpw(plainText, encryptedPassword)
}
