package jaina.intellij.plugin.processor;

/**
 * Created by kuma on 2018/9/7.
 */

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import jaina.intellij.plugin.model.LombokProblem;
import jaina.intellij.plugin.model.ProblemBuilder;
import jaina.intellij.plugin.model.ProblemEmptyBuilder;
import jaina.intellij.plugin.model.ProblemNewBuilder;
import jaina.intellij.plugin.util.PsiAnnotationSearchUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base lombok processor class for class annotations
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractClassProcessor extends AbstractProcessor implements Processor {

    protected AbstractClassProcessor(@NotNull Class<? extends PsiElement> supportedClass,
                                     @NotNull Class<? extends Annotation> supportedAnnotationClass) {
        super(supportedClass, supportedAnnotationClass);
    }

    protected AbstractClassProcessor(@NotNull Class<? extends PsiElement> supportedClass,
                                     @NotNull Class<? extends Annotation> supportedAnnotationClass,
                                     @NotNull Class<? extends Annotation>... equivalentAnnotationClasses) {
        super(supportedClass, supportedAnnotationClass, equivalentAnnotationClasses);
    }

    @NotNull
    @Override
    public List<? super PsiElement> process(@NotNull PsiClass psiClass) {
        List<? super PsiElement> result = Collections.emptyList();
        PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, getSupportedAnnotationClasses());
        if (null != psiAnnotation) {
            if (validate(psiAnnotation, psiClass, ProblemEmptyBuilder.getInstance())) {
                result = new ArrayList<PsiElement>();
                generatePsiElements(psiClass, psiAnnotation, result);
            }
        }
        return result;
    }

    @NotNull
    public Collection<PsiAnnotation> collectProcessedAnnotations(@NotNull PsiClass psiClass) {
        Collection<PsiAnnotation> result = new ArrayList<PsiAnnotation>();
        PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, getSupportedAnnotationClasses());
        if (null != psiAnnotation) {
            result.add(psiAnnotation);
        }
        return result;
    }

    @NotNull
    @Override
    public Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation) {
        Collection<LombokProblem> result = Collections.emptyList();
        // check first for fields, methods and filter it out, because PsiClass is parent of all annotations and will match other parents too
        PsiElement psiElement = PsiTreeUtil.getParentOfType(psiAnnotation, PsiField.class, PsiMethod.class, PsiClass.class);
        if (psiElement instanceof PsiClass) {
            ProblemNewBuilder problemNewBuilder = new ProblemNewBuilder();
            validate(psiAnnotation, (PsiClass) psiElement, problemNewBuilder);
            result = problemNewBuilder.getProblems();
        }

        return result;
    }

    protected abstract boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder);

    protected abstract void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target);

    private String calcNewPropertyValue(Collection<String> allProperties, String fieldName) {
        String result = null;
        final Collection<String> restProperties = new ArrayList<String>(allProperties);
        restProperties.remove(fieldName);

        if (!restProperties.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            builder.append('{');
            for (final String property : restProperties) {
                builder.append('"').append(property).append('"').append(',');
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append('}');

            result = builder.toString();
        }
        return result;
    }

}
