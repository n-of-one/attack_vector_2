package org.n1.av2.backend.security

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.constraintvalidation.SupportedValidationTarget
import jakarta.validation.constraintvalidation.ValidationTarget
import kotlin.reflect.KClass


@Pattern(regexp = "[\\p{L}\\p{N}\\p{P}\\p{M}\\p{S}\\p{Z}\\n\\r]*")
@Size(min = 0, max = 100)

@MustBeDocumented
@Constraint(validatedBy = [SafeStringValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SafeString(
    val message: String = "Invalid characters in string",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)


@Pattern(regexp = "[\\p{L}\\p{N}\\p{P}\\p{M}\\p{S}\\p{Z}\\n\\r]*")
@Size(min = 0, max = 4000)
@MustBeDocumented
@Constraint(validatedBy = [SafeJwtValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SafeJwt(
    val message: String = "Invalid characters in jwt",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

// Without this class, the @Pattern and @Size annotations are ignored
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
class SafeStringValidator : ConstraintValidator<SafeString, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext)= true
}
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
class SafeJwtValidator : ConstraintValidator<SafeJwt, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext) = true
}
