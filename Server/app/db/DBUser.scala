package db

import anorm.NamedParameter
import models.User

object DBUser {
  private val helper = new AnormHelper("expense_list")
  private val idColumn = 'user_id

  def toData(user: User, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      'phone -> user.phone,
      'email -> user.email,
      'password -> user.password,
      'registration_date -> user.registerDate)

    if (withId)
      values :+ new NamedParameter(idColumn.name, user.userId)
    else
      values
  }

  def save(user: User) = helper.insert(toData(user, withId = true))
  def update(user: User) = helper.update(toData(user, withId = false), Seq(idColumn -> user.userId))
  def delete(id: Long) = helper.delete(Seq(idColumn -> id))
}