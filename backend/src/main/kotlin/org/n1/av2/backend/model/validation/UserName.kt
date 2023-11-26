package org.n1.av2.backend.model.validation

import jakarta.validation.Constraint
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass


@MustBeDocumented
@Constraint(validatedBy = [])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "[a-zA-Z0-9_-]+", message = "Usernames are 3-10 characters, alphanumeric and - and _")
@Size(min = 3, max = 10, message = "Usernames are 3-10 characters, alphanumeric and - and _")
annotation class UserName(
    val message: String = "",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)