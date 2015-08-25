package com.iheart.models

import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction._
import com.iheart.util.VclUtils._
import com.iheart.util.VclUtils.VclActionType._
import play.Logger


trait ModelValidations {

  type ValidationError = String
  type Validation = Either[ValidationError,Boolean]

  def isValid(validations: Seq[Validation]): Either[Seq[ValidationError],Boolean]  = {

    def process(validations: Seq[Validation], errors: Seq[ValidationError] = Seq()): Seq[ValidationError]  = validations match {
      case h :: t if h.isRight => process(t,errors)
      case h :: t if h.isLeft => process(t,  errors :+ h.left.getOrElse("Validation error"))
      case _ => errors
    }

    process(validations) match {
      case Seq() => Right(true)
      case x => Left(x)
    }
  }

  implicit class validationToEither(b: Boolean) {
    def toValidate(s: String): Validation = b match {
      case true => Right(true)
      case false => Left(s)
    }
  }

  def validCondition(key: String): Validation =
    conditionMap.get(key).isDefined.toValidate("Invalid condition key " + key)


  def validMatcher(key: String): Validation =
    vclMatcherMap.get(key).isDefined.toValidate("Invalid matcher " + key + " specified")


  def validAction(key: String): Validation =
    actionMap.get(key).isDefined.toValidate("Invalid action key of " + key)

  //Ensure there is only 1 action declared as SingleVal
  def validateSingleAction(actions: Seq[RuleAction]): Validation =
    (actions.count(a => a.action.actionType == SingleAction) < 2).toValidate("Only a single action of type SingleAction is permitted")

  //validate the matchType
  def validMatchType(m: String): Validation =
    (m == "ANY" || m == "ALL").toValidate("Invalid matcher type of " + m)

  def validateNameValAction(actions: Seq[RuleAction]) =
    (actions.count(a => a.action.actionType == NameValAction && (a.name.isEmpty || a.value.isEmpty)) == 0).toValidate("NameVal actions must have name and value")

  def validateNameAction(actions: Seq[RuleAction]) =
    (actions.count(a => a.action.actionType == NameAction && a.name.isEmpty) == 0).toValidate("actions of type NameAction must have a name")

  def validateBoolAction(actions: Seq[RuleAction]) =
    (actions.count(a => a.action.actionType == Bool && (a.value.isEmpty || (a.value.get.toInt != 0 && a.value.get.toInt != 1))) == 0).toValidate("Boolean action type requires value to be either 0 or 1 ")

  def validUnits(units: Option[String]) = (!(units.isDefined && vclUnitMap.get(units.get.toLowerCase).isEmpty )).toValidate("Invalid units " + units.getOrElse(""))
}