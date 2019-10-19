package security
import java.security.{MessageDigest, SecureRandom}

import scala.annotation.tailrec

/**
 * Generates a Bearer Token with a length of
 * 32 characters (MD5) or 64 characters (SHA-256) according to the
 * specification RFC6750 (http://tools.ietf.org/html/rfc6750)
 *
 * Uniqueness obtained by hashing system time combined with a
 * application supplied 'token-prefix' such as a unique username or a userId
 *
 * public methods:
 *  generateMD5Token(tokenPrefix: String): String
 *  generateSHAToken(tokenPrefix: String): String
 **/

  object    BearerTokenGenerator {

  val TOKEN_LENGTH = 45	// TOKEN_LENGTH is not the return size from a hash,
  // but the total characters used as random token prior to hash
  // 45 was selected because System.nanoTime().toString returns
  // 19 characters.  45 + 19 = 64.  Therefore we are guaranteed
  // at least 64 characters (bytes) to use in hash, to avoid MD5 collision < 64


  /*
   *The generated token has enough entropy : 45 random characters 0-9 a-z A-Z,
   *  So no need to use salt , we can calculate an unsalted fast hash and store it .
   *  This is safe, because it is not possible to successfully brute-force such token. Unlike passwords which we stored in DB using bcrypt
   * because passwords chosen by people are often relatively weak, since they have to be remembered
   *
   *
   */

  val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.-"
  val secureRandom = new SecureRandom()

    def toHex(bytes: Array[Byte]): String = bytes.map( "%02x".format(_) ).mkString("")

   private def sha(s: String): String = {
    toHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8")))
  }



     def md5(s: String): String = {
    toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))
  }

     private def generateToken(tokenLength: Int) : String = {
    val charLen = TOKEN_CHARS.length()
    @tailrec
    def generateTokenAccumulator(accumulator: String, number: Int) : String = {
      if (number == 0) return accumulator
      else
        generateTokenAccumulator(accumulator + TOKEN_CHARS(secureRandom.nextInt(charLen)).toString, number - 1)
    }
    generateTokenAccumulator("", tokenLength)
  }



  def generateMyToken(tokenPrefix : String) = {
    tokenPrefix+ System.nanoTime() + generateToken(TOKEN_LENGTH)
  }







}
