package com.apollographql.apollo3.compiler.codegen.java.file

import com.apollographql.apollo3.compiler.codegen.java.CodegenJavaFile
import com.apollographql.apollo3.compiler.codegen.java.JavaClassBuilder
import com.apollographql.apollo3.compiler.codegen.java.JavaContext
import com.apollographql.apollo3.compiler.codegen.typeUtilPackageName
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.element.Modifier

internal class UtilAssertionsBuilder(val context: JavaContext) : JavaClassBuilder {
  override fun prepare() {}

  override fun build(): CodegenJavaFile {
    return CodegenJavaFile(
        packageName = context.layout.typeUtilPackageName(),
        typeSpec = TypeSpec
            .classBuilder(context.layout.topLevelName("Assertions"))
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("assertOneOf")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(
                        ArrayTypeName.of(
                            ParameterizedTypeName.get(
                                ClassName.get("com.google.common.base", "Optional"),
                                WildcardTypeName.subtypeOf(
                                    ParameterizedTypeName.get(
                                        ClassName.get("com.google.common.base", "Optional"),
                                        WildcardTypeName.subtypeOf(Object::class.java)
                                    )
                                )
                            )
                        ),
                        "args",
                    )
                    .varargs(true)
                    .addAnnotation(SafeVarargs::class.java)
                    .addCode(
                        """
                          int presentArgs = 0;
                          for (Optional<? extends Optional<?>> arg : args) {
                            if (arg.isPresent()) {
                              presentArgs++;
                            }
                          }
                          if (presentArgs != 1) {
                            throw new IllegalArgumentException("@oneOf input must have one field set (got " + presentArgs + ")");
                          }
                          if (!args[0].get().isPresent()) {
                            throw new IllegalArgumentException("The value set on @oneOf input field must be non-null");
                          }
                        """.trimIndent()
                    )
                    .build()
            )
            .build()
    )
  }
}
