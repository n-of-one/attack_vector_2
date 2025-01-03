package org.n1.av2.platform.inputvalidation

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
@Pattern(regexp = "[a-zA-Z0-9_/-]+")
@Size(min = 1, max = 80, ) // long enough for: /x/<encoded app path>
annotation class LoginRedirectParam(
    val message: String = "",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)
