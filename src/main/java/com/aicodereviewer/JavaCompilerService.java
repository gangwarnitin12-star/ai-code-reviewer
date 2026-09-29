package com.aicodereviewer;

import org.springframework.stereotype.Service;

import javax.tools.*;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Service
public class JavaCompilerService {

    public CompileResult compile(String code) {

        JavaCompiler compiler =
                ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            return new CompileResult(
                    false,
                    "Java compiler is not available on the server.",
                    List.of()
            );
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        JavaFileObject source =
                new JavaSourceFromString("Main", code);

        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(
                             diagnostics,
                             Locale.ENGLISH,
                             null
                     )) {

            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            List.of("-proc:none"),
                            null,
                            List.of(source)
                    );

            boolean success =
                    Boolean.TRUE.equals(task.call());

            List<String> errors =
                    diagnostics.getDiagnostics()
                            .stream()
                            .filter(d ->
                                    d.getKind() == Diagnostic.Kind.ERROR)
                            .map(this::formatDiagnostic)
                            .toList();

            if (success) {
                return new CompileResult(
                        true,
                        "Compilation Successful",
                        errors
                );
            }

            return new CompileResult(
                    false,
                    "Compilation Failed",
                    errors
            );

        } catch (Exception e) {
            return new CompileResult(
                    false,
                    "Compiler error: " + e.getMessage(),
                    List.of()
            );
        }
    }

    private String formatDiagnostic(
            Diagnostic<? extends JavaFileObject> diagnostic) {

        return "Line "
                + diagnostic.getLineNumber()
                + ": "
                + diagnostic.getMessage(Locale.ENGLISH);
    }

    public record CompileResult(
            boolean success,
            String message,
            List<String> errors
    ) {
    }

    private static class JavaSourceFromString
            extends SimpleJavaFileObject {

        private final String code;

        JavaSourceFromString(
                String className,
                String code) {

            super(
                    URI.create(
                            "string:///"
                                    + className
                                    + Kind.SOURCE.extension
                    ),
                    Kind.SOURCE
            );

            this.code = code;
        }

        @Override
        public CharSequence getCharContent(
                boolean ignoreEncodingErrors) {

            return code;
        }
    }
}