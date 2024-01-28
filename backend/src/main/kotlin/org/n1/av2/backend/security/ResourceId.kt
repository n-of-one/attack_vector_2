package org.n1.av2.backend.security

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraintvalidation.SupportedValidationTarget
import jakarta.validation.constraintvalidation.ValidationTarget
import kotlin.reflect.KClass


@Pattern(regexp = "site-[a-f0-9]{4}-[a-f0-9]{4}")
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidSiteIdValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
annotation class ValidSiteId(
    val message: String = "Invalid id",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Pattern(regexp = "node-[a-f0-9]{4}-[a-f0-9]{4}")
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidNodeIdValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
annotation class ValidNodeId(
    val message: String = "Invalid id",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Pattern(regexp = "node-[a-f0-9]{4}-[a-f0-9]{4}:layer-[a-f0-9]{4}")
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidLayerIdValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
annotation class ValidLayerId(
    val message: String = "Invalid id",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

// Without this class, the @Pattern annotations is ignored
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
class ValidSiteIdValidator : ConstraintValidator<ValidSiteId, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext) = true
}

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
class ValidNodeIdValidator : ConstraintValidator<ValidNodeId, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext) = true
}

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
class ValidLayerIdValidator : ConstraintValidator<ValidLayerId, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext) = true
}
